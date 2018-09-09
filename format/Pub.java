package format;

import it.unisa.dia.gas.jpbc.*;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.digests.SHA256Digest;

public class Pub {
	/*
	 * Public key
	 */
	public Pairing p;				
	public Element g;				/* G_1 */
	public Element g_hat_alpha;		/* G_T */
	public Element g_a;  			/* G_1 */
	public double SecurityDegree;
	
	//used in test
	public Element alpha;
	public Element a;
	//---------used in test end
	
	
	public Element H(String attr){
		/*
		 * reference: Hashing a message into an elliptic curve group
		 * http://blog.ruchith.org/2010/11/hashing-message-into-elliptic-curve.html
		 */
		byte[] data=attr.getBytes();
		
		//Create a SHA-256 of the message
        SHA256Digest dgst = new SHA256Digest();
        dgst.reset();
        dgst.update(data, 0, data.length);
        int digestSize = dgst.getDigestSize();
        byte[] hash = new byte[digestSize];
        dgst.doFinal(hash, 0);
        Element mul = p.getG1().newElement().setFromHash(hash, 0, hash.length).getImmutable();
        System.out.println("map the string "+attr+" to point in the group, where point = "+mul);
        
		return mul.duplicate();
	}
}
