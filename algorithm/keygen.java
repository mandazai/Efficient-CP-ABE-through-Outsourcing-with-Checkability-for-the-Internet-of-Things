package algorithm;

import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.lang.Override;
import format.Msk;
import format.Prv;

public class keygen {
	public Prv prv = new Prv();
	
	public keygen(Msk msk,String attr_str){
		System.out.println("-------------------Key Generation Algorithm----------------------");
		System.out.println("input : MSK, attribute set S");
		System.out.println("output: transformation key TK and the private key SK");
		/* parse Attribute */
		StringTokenizer st = new StringTokenizer(attr_str);
		String res[];
		int len;

		while (st.hasMoreTokens()) {
			prv.tk.str_attr.add(st.nextToken(":"));
		}
		Collections.sort(prv.tk.str_attr, new SortByAlphabetic());

		len = prv.tk.str_attr.size();
		res = new String[len];
		for (int i = 0; i < len; i++)
			res[i] = prv.tk.str_attr.get(i);
		
		/*generate SK and TK*/
		prv.Generate_sk(msk,res);
		

		//System.out.println("keygen1 "+prv.tk.PK.p.pairing( prv.tk.PK.p.getG1().newElement().setToRandom().duplicate().powZn(prv.tk.PK.p.getZr().newElement().setToRandom()) , prv.tk.L));
		//System.out.println("keygen2 "+prv.tk.PK.p.pairing( prv.tk.PK.p.getG1().newElement().setToRandom().duplicate().powZn(prv.tk.PK.p.getZr().newElement().setToRandom()) , prv.tk.K));
	}
	
	/*Override the sort of collection */
	static class SortByAlphabetic implements Comparator<String> {
		@Override
		public int compare(String s1, String s2) {
			if (s1.compareTo(s2) >= 0)
				return 1;
			return 0;
		}
	}
}
