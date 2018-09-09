package algorithm;

import java.util.ArrayList;
import java.util.List;

import format.Cph;
import format.PartCph;
import format.Pub;
import format.TK;
import it.unisa.dia.gas.jpbc.Element;

public class trans {
	/*
	 * transform algorithm
	 * input : TK,CT
	 * output: partial cipher text CT'
	 */
	public PartCph partialCT= new PartCph();
	public void trans(TK tk,Cph cph) {
		System.out.println("-------------------transform----------------------");
		Element temp0;
		Element temp1;
		Element temp2;
		
		int i,j;
		
		partialCT.c=cph.c.duplicate();
		partialCT.g_hat_alpha=tk.PK.p.getGT().newOneElement();
		
		System.out.println("check weather the set s in the input tk is satisfies the access tree which is belong to the input cph");
		if(!cph.AT.check_attr(cph.AT.ThresholdAT,tk.str_attr)) {
			System.out.println("error	: the attribite dose not satisfied the access structure that you have not access to decrypt the cph.");
		}
		else {
			System.out.println("generate the anti-matrix W");
			System.out.println("AT="+cph.AT.ThresholdAT);
			System.out.println("attr="+tk.str_attr);
			cph.AT.W_ini();
			cph.AT.find_anti(cph.AT.ThresholdAT,tk.str_attr,0,1);
			cph.AT.printfMatrix_W();
			
			cph.AT.W2_ini(tk.PK);
			cph.AT.printfMatrix_W2();
			cph.AT.W2_Gaussian();
			cph.AT.printfMatrix_W();
			cph.AT.printfMatrix_W2();
			
			
			cph.AT.getFirstW2(tk.PK);
			cph.AT.printfMatrix_W3();
			
			cph.AT.getW4W5W6();
			cph.AT.printfMatrix_W4();
			cph.AT.printfMatrix_W5();
			cph.AT.printfMatrix_W6();
			//initialize
			j=0;
			temp0=tk.PK.p.getG1().newOneElement();
			temp1=tk.PK.p.getGT().newOneElement();
			temp2=tk.PK.p.getGT().newOneElement();
			
			for(i=0;i<cph.AT.lo.size();i++) {
				//if the attr is satisfied
				if(cph.AT.W5.get(j)==i) {
					//Ci^wi
					temp0.mul(cph.Ci.get(i).duplicate().powZn(cph.AT.W4.get(j).duplicate()));
					//mul e(Di^wi,K)
					temp2.mul( tk.PK.p.pairing( cph.Di.get(i).duplicate().powZn(cph.AT.W4.get(j).duplicate()) , tk.comps.get(cph.AT.W6.get(j))));
					j++;
					if(j==cph.AT.W5.size())
						break;
				}
			}
			//e(Ci^wi,L)
			temp1=tk.PK.p.pairing(temp0.duplicate(),tk.L.duplicate());
			//e(C',K')
			partialCT.g_hat_alpha=tk.PK.p.pairing(cph.g_s.duplicate(),tk.K.duplicate());
			
			//------test
			Element temp3;
			Element temp4;
			
			/*
			System.out.println("partialCT.g_hat_alpha="+partialCT.g_hat_alpha);
			temp3=tk.PK.p.pairing(tk.PK.g.duplicate(),tk.PK.g.duplicate()) .powZn( cph.test_s.duplicate().mulZn(tk.PK.alpha.duplicate()).div(tk.test_z.duplicate()) );
			temp4=tk.PK.p.pairing(tk.PK.g.duplicate(),tk.PK.g.duplicate()) .powZn( tk.PK.a.duplicate()   .mulZn(cph.test_s.duplicate()) .mul(tk.test_t.duplicate()) );
			System.out.println(temp3.duplicate().mul(temp4.duplicate()));
			
			System.out.println("g = "+tk.PK.g.duplicate());
			System.out.println("s = "+cph.test_s.duplicate());
			System.out.println("al= "+tk.PK.alpha.duplicate());
			System.out.println("z = "+tk.test_z.duplicate());
			System.out.println("a = "+tk.PK.a.duplicate());
			System.out.println("t = "+tk.test_t.duplicate());
			*/
			
			/*
			//test for C',K
			System.out.println("C' = "+cph.g_s);
			System.out.println("C' = "+tk.PK.g.duplicate().powZn(cph.test_s.duplicate()));
			System.out.println("K  = "+tk.K);
			System.out.println("K  = "+tk.PK.g.duplicate().powZn(tk.PK.alpha.duplicate().div(tk.test_z.duplicate())).mul(tk.PK.g_a.duplicate().powZn(tk.test_t.duplicate())));
			*/
			
			
			
			//e(C',K)!=e(g,g)^(s*alpha/z).mul( e(g,g)^ast )
			temp3=tk.PK.p.getGT().newZeroElement();
			temp3.add(tk.PK.g_hat_alpha.duplicate().powZn(cph.test_s.duplicate().div(tk.test_z.duplicate())));
			temp3.mul(tk.PK.p.pairing(tk.PK.g_a.duplicate(),tk.PK.g.duplicate()).powZn(cph.test_s.duplicate().mul(tk.test_t.duplicate())));
			
			
			System.out.println("e(C',K) = "+partialCT.g_hat_alpha);
			System.out.println("e(C',K)'= "+temp3);
			System.out.println("PK.g_hat_alpha = "+tk.PK.g_hat_alpha);
			System.out.println("PK.g_hat_alpha = "+tk.PK.p.pairing(tk.PK.g.duplicate(),tk.PK.g.duplicate()).powZn(tk.PK.alpha.duplicate()));
			
			
			/*
			//test for the attributes satisfied, £U(w(i)*lambda(i))=s success
			temp3=tk.PK.p.getZr().newZeroElement();
			j=0;
			for(i=0;i<cph.test_lambda.size();i++) {
				
				if(i==cph.AT.W5.get(j)) {
					temp3.add(cph.AT.W4.get(j).duplicate().mulZn(cph.test_lambda.get(i).duplicate()));
					j++;
					if(j==cph.AT.W5.size())
						break;
				}
			}
			
			temp4=tk.PK.p.getZr().newOneElement();
			for(i=0;i<cph.AT.W4.size();i++) {
				temp4.mulZn(cph.AT.W4.get(i).duplicate());
				temp4.mulZn(cph.test_lambda.get(cph.AT.W5.get(i)).duplicate());
			}
			
			System.out.println("s = "+temp3);
			System.out.println("s = "+temp4);
			System.out.println("s = "+cph.test_s);
			*/
			
			//test for the attributes satisfied, £U(w(i)*lambda(i))=s success
			/*
			temp3=tk.PK.p.getGT().newOneElement();
			j=0;
			for(i=0;i<cph.test_lambda.size();i++) {
				if(i==cph.AT.W5.get(j)) {
					temp3.mul(tk.PK.p.pairing(tk.PK.g.duplicate(),tk.PK.g.duplicate()).powZn(tk.test_t.duplicate().mul(tk.PK.a.duplicate()).mul(cph.AT.W4.get(j).duplicate().mulZn(cph.test_lambda.get(i).duplicate()))));
					j++;
					if(j==cph.AT.W5.size())
						break;
				}
			}
			*/
			temp4=tk.PK.p.getGT().newOneElement();
			j=0;
			for(i=0;i<cph.AT.lo.size();i++) {
				if(i==cph.AT.W5.get(j)) {
					temp4.mul(tk.PK.p.pairing(tk.PK.g.duplicate(),tk.PK.g.duplicate()).powZn(tk.test_t.duplicate().mul(tk.PK.a.duplicate()).mul(cph.AT.W4.get(j).duplicate().mulZn(cph.test_lambda.get(i).duplicate()))));
					j++;
					if(j==cph.AT.W5.size())
						break;
				}
			}
			/*
			for(i=0;i<;i++) {
				//if the attr is satisfied
				if(cph.AT.W5.get(j)==i) {
					//Ci^wi
					temp0.mul(cph.Ci.get(i).duplicate().powZn(cph.AT.W4.get(j).duplicate()));
					//mul e(Di^wi,K)
					temp2.mul( tk.PK.p.pairing( cph.Di.get(i).duplicate().powZn(cph.AT.W4.get(j).duplicate()) , tk.comps.get(cph.AT.W6.get(j))));
					j++;
					if(j==cph.AT.W5.size())
						break;
				}
			}
			*/
			System.out.println("g_hat_alpha     ="+temp4);
			System.out.println("g_hat_alpha     ="+temp1);
			
			//------------------test end
			
			
			partialCT.g_hat_alpha.div( temp1.duplicate().mul(temp2.duplicate()) );
			System.out.println();
			System.out.println("temp1           ="+temp1.duplicate());
			System.out.println("temp2           ="+temp2.duplicate());
			System.out.println("temp1.mul(temp2)="+temp1.duplicate().mul(temp2.duplicate()));
			System.out.println("g_hat_alpha     ="+partialCT.g_hat_alpha);
			System.out.println("g_hat_alpha     ="+temp3.duplicate().div(temp4.duplicate()));
			System.out.println("Dec_Msg': "+partialCT.c.duplicate().div(temp3.duplicate().div(temp4.duplicate())).powZn(tk.test_z.duplicate()));
		}
	}
}
