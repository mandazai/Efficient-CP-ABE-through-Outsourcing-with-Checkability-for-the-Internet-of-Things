package format;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;

public class TK {
	/*
	 * A Transfer key
	 */
	public Pub PK;
	public Element K;  					/* G_1 */
	public Element L;  					/* G_1 */
	public ArrayList<Element> comps = new ArrayList<Element>();		/* PrvComp */
	public ArrayList<String> str_attr = new ArrayList<String>();	/*record attributes*/
	
	//used in test
	public Element test_t;
	public Element test_z;
	//---------used in test end
}
