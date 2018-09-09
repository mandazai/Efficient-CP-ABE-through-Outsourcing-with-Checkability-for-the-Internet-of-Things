package format;

import it.unisa.dia.gas.jpbc.Element;
import java.util.ArrayList;

public class Prv {
	/*
	 * A private key
	 */
	public Element z; 		/* G_1 */
	public TK tk = new TK();
	
	Element t;
	
	public void Generate_sk (Msk msk,String[] S){
		t = msk.PK.p.getZr().newRandomElement();
		Element comp;
		TK sk=new TK();
		sk.PK=msk.PK;
		sk.L=msk.PK.g.duplicate().powZn(t.duplicate());
		sk.K=msk.g_alpha.duplicate().mul(msk.PK.g_a.duplicate().powZn(t.duplicate()));
		
		int i, len = S.length;
		sk.comps = new ArrayList<Element>();
		
		for (i = 0; i < len; i++) {
			comp = msk.PK.H(S[i]).duplicate();
			sk.comps.add(comp.duplicate());
		}
		Generate_tk(sk);
	}
	
	public void Generate_tk (TK sk){
		Element e;
		Element comp;
		e = sk.PK.p.getZr().newOneElement();
		z = sk.PK.p.getZr().newRandomElement();
		
		tk.test_z=z.duplicate();
		
		tk.PK = sk.PK;
		tk.K = sk.K.duplicate().powZn(e.duplicate().div(z.duplicate()));
		tk.L = sk.L.duplicate().powZn(e.duplicate().div(z.duplicate()));
		tk.test_t=t.duplicate().div(z.duplicate());
		
		int i, len = sk.comps.size();
		
		for (i = 0; i < len; i++) {
			comp = sk.comps.get(i).duplicate().powZn(e.duplicate().div(z.duplicate()));
			tk.comps.add(comp.duplicate());
		}
	}
}
