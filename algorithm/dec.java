package algorithm;

import format.Cph;
import format.Prv;
import it.unisa.dia.gas.jpbc.Element;

public class dec {
	/*
	 * decryption algorithm
	 * input : SK, CT
	 * output: Message
	 */
	public Element Message;
	
	public dec(Prv sk,Cph cph) {
		System.out.println("-------------------decryption----------------------");
		trans Proxy=new trans();
		Element Message;
		Element one=sk.tk.PK.p.getGT().newOneElement();
		int i;
		//initialize
		for(i=0;i<cph.AT.W.size();i++) {
			cph.AT.W.get(i).clear();
		}cph.AT.W.clear();
		for(i=0;i<cph.AT.W2.size();i++) {
			cph.AT.W2.get(i).clear();
		}cph.AT.W2.clear();
		cph.AT.W3.clear();
		cph.AT.W4.clear();
		cph.AT.W5.clear();
		cph.AT.W6.clear();
		
		System.out.println("deliver CT to proxy to generate partial CT");
		Proxy.trans(sk.tk,cph);
		
		//if g_hat_alpha have the original value
		if(Proxy.partialCT.g_hat_alpha.duplicate().equals(one)) {
			System.out.println("error	: some error when generate partial cph.");
		}else {
			Message=Proxy.partialCT.c.duplicate().div(Proxy.partialCT.g_hat_alpha.duplicate().powZn(sk.z.duplicate()));
			System.out.println("Dec_Msg	: "+Message);
			System.out.println("Ori_Msg	: "+cph.ori_msg);
			if(!cph.ori_msg.duplicate().equals(Message.duplicate()))
				System.out.println("the message is different from original one.");
		}
		
		
	}
}
