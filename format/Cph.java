package format;

import java.util.ArrayList;
import java.util.List;

import it.unisa.dia.gas.jpbc.Element;

public class Cph {
	/*
	 * A ciphertext.
	 */
	public Element c;	/* G_T */
	public Element g_s;	/* G_1 */
	public ArrayList<Element> Ci=  new ArrayList<Element>();	/* G_1 */
	public ArrayList<Element> Di=  new ArrayList<Element>();	/* G_1 */
	//public ArrayList<CphComps> Comps=  new ArrayList<CphComps>();
	public LsssAccessStructure AT;
	public Element ori_msg;	/* G_1 */
	
	//used in test
	public Element test_s;
	public List<Element> test_lambda= new ArrayList<Element>();
	//---------used in test end
	
}
