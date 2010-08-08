package net.robig.stlab.midi;

import net.robig.logging.Logger;

public abstract class AbstractMidiCommand implements IMidiCommand {

	public static final String command_start_data=COMMAND_START.replaceAll(" ", "").toLowerCase();
	public static final String command_end_data=COMMAND_END.replaceAll(" ", "").toLowerCase();
	
	public enum State { NEW, INIT, EXECUTED, FINISHED, WAIT, ARRIVED };
	protected Logger log = new Logger(this.getClass());
	protected String functionCode = "40";
	protected String expectedReturnCode = "23"; // Data Load completed
	protected boolean expectData=false;
	protected String resultData=null;
	protected boolean ranSuccessfully=false;
	protected State state = State.NEW;
	protected MidiController controller = MidiController.getInstance();
	
	@Override
	abstract public void run();
	
	@Override
	public void prepare() {
		state=State.INIT;	
	}
	
	@Override
	public void finished() {
		state=State.FINISHED;
	}
	
	public synchronized void waitForResult() {
		while(isRunning()){
			log.debug("Waiting for answer of command: "+this);
			try {
				//log.debug("waiting...");
				state=State.WAIT;
				wait();
				//log.debug("wait ende");
			} catch (InterruptedException e) {
				e.printStackTrace(log.getDebugPrintWriter());
			}
		}
	}
	
	private synchronized void arrived() {
		state=State.ARRIVED;
		log.debug("notify waiting thread");
		this.notify();
	}
	
	
	public synchronized boolean isRunning() {
//		synchronized (state) {
			log.debug("isRunning: "+state);
			if(state.equals(State.ARRIVED))
				return false;
//		}
		return true;
	}
	
	@Override
	public void receive(String data) throws MidiCommunicationException {
		ranSuccessfully=analyzeResult(data);
		arrived();
		if(!ranSuccessfully()) throw new MidiCommunicationException("unexpected midi return code!",data);
		log.info("Midi command successful");
	}
	
	/**
	 * analyze incoming data, check against expectations
	 * @param data
	 * @return true if data is like expected, false if not
	 */
	protected boolean analyzeResult(String data){
		if(expectData) {
			if(!data.startsWith(command_start_data+expectedReturnCode)){
				log.debug("expected return code: "+expectedReturnCode+" but got data: "+data);
				return false;
			}
			String resultData=data.substring(command_start_data.length()+expectedReturnCode.length(),data.length()-command_end_data.length());
			log.debug("received data: "+resultData);
			receiveData(resultData);
			return true;
		}
		boolean ret=data.equals(command_start_data+expectedReturnCode+command_end_data);
		if(!ret) log.debug("expected return code: "+expectedReturnCode+" but got data: "+data);
		return ret;
	}

	/**
	 * Overwrite this method to process received data
	 * @param resultData
	 */
	protected void receiveData(String resultData){
		this.resultData=resultData;
	}
	
	/**
	 * get the command result data
	 * @return
	 */
	public String getResultData() {
		return resultData;
	}
	
	public boolean ranSuccessfully() {
		return this.ranSuccessfully;
	}
	
	protected void sendData(String data){
		controller.sendMessage(COMMAND_START+functionCode+data+COMMAND_END);
	}

	static public String toHexString(int i){
		return MidiController.toHexString(i);
	}
}
