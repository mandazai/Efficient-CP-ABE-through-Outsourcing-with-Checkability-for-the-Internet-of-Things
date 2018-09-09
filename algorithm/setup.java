package algorithm;
import format.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
public class setup {
	public Pub pub = new Pub();
	public Msk msk = new Msk();
	public setup(String SP, String U, double SecurityDegree) {
		System.out.println("-------------------Setup Algorithm----------------------");
		System.out.println("input : a security parameter SK and a universe description U");
		System.out.println("output: master secret key MSK and public parameters PK");
		Element alpha, a;
		
		/*
		 * dynamic generate the bilinear group
		 * reference: http://www.cnblogs.com/yanspecial/p/5613342.html
		 */
		//Specified the type of elliptic curve
		//Type A pairings are constructed on the curve  y^2=x^3+x
		//160 is the bits length in the Zp and 512 is the bits length in the G
		TypeACurveGenerator pg = new TypeACurveGenerator(160,512);
		//generate the parameters of the elliptic curve
		PairingParameters typeAParams = pg.generate();
		//For bilinear maps only, to use the PBC wrapper and gain in performance, the usePBCWhenPossible property of the pairing factory must be set.
		PairingFactory.getInstance().setUsePBCWhenPossible(true);
		//initialize the pairing
		pub.p= PairingFactory.getPairing(typeAParams);
		//initialize type of parameters
		alpha = pub.p.getZr().newRandomElement();
		a = pub.p.getZr().newRandomElement();
		pub.g = pub.p.getG1().newRandomElement();
		pub.SecurityDegree = SecurityDegree;
		
		//used in test
		pub.alpha=alpha.duplicate();
		pub.a=a.duplicate();
		//---------used in test end
		
		//compute g^a
		pub.g_a = pub.g.duplicate().powZn(a.duplicate());
		//compute e(g,g)^a
		pub.g_hat_alpha = pub.p.pairing(pub.g.duplicate(), pub.g.duplicate()).powZn(alpha.duplicate());
		//MSK=(g^alpha,PK)
		msk.g_alpha = pub.g.duplicate().powZn(alpha.duplicate());
		msk.PK=pub;
		
		System.out.println(typeAParams);
		System.out.println("MSK: (");
		System.out.printf("  %-7s = "+msk.g_alpha+"\n","g^alpha");
		System.out.printf("  %-7s = (\n","PK");
		System.out.printf("    %-7s = "+pub.p+"\n","pub.p");
		System.out.printf("    %-7s = "+alpha+"\n","alpha");
		System.out.printf("    %-7s = "+a+"\n","a");
		System.out.printf("    %-7s = "+pub.g+"\n","g");
		System.out.printf("    %-7s = "+pub.g_a+"\n","g^a");
		System.out.printf("    %-7s = "+pub.g_hat_alpha+"\n","e(g,g)^alpha");
		System.out.printf("    %-7s = "+pub.SecurityDegree+"\n","Security Degree");
		System.out.println("    )");
		System.out.println(")");
		
		
		
	}
}
