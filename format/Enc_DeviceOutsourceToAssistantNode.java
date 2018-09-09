package format;

import java.util.ArrayList;
import java.util.List;

import it.unisa.dia.gas.jpbc.Element;

public class Enc_DeviceOutsourceToAssistantNode {
	/*
	 * Used when device outsourcing data to assistant node in the encryption phase.
	 */
	//Record List
	//index = 0 > s
	//index=  1 > r
	//index = 2 > lambda
	public int ID ;
	//Store the Outsource Computation
	public List<Element> s = new ArrayList<Element>();
	public List<Element> r = new ArrayList<Element>();
	public List<Element> lambda = new ArrayList<Element>();
	public List<Element> attr_H = new ArrayList<Element>();
}
