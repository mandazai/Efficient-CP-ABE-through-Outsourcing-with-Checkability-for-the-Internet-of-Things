package format;

import java.math.BigInteger;
import java.util.*;

import it.unisa.dia.gas.jpbc.Element;

public class LsssAccessStructure{
	/*
	 * this class store a Matrix and list lo to record the rows of M to attributes.
	 * ( M, lo ) is call an LSSS access structure.
	 * 
	 * LSSS(string s)	algorithm convert a threshold Access tree string to Linear Access tree matrix
	 * generate_lo()	algorithm random the index of the Linear Access tree matrix
	 * printfMatrix()	algorithm to print the Matrix
	 * SplitString(String s)	algorithm to split the threshold Access tree string string to it child node
	 * printfList(List<String> L)	algorithm to print a list
	 * WheatherTheThresholdString(String s)	distinct whether the string is belong to threshold Access tree string
	 */
	
	public int row;
	public int column;
	//LSSS matrix M
	public List<List<Integer>> M = new ArrayList<List<Integer>>();
	//anti-matrix of M
	public List<List<Integer>> W = new ArrayList<List<Integer>>();
	//element matrix used in Gaussian Elimination
	public List<List<Element>> W2 = new ArrayList<List<Element>>();
	//first row of W2 (Representative "s")
	public List<Element> W3 = new ArrayList<Element>();
	//Sort W3 by attribute
	public List<Element> W4 = new ArrayList<Element>();
	//record the corresponding attribute of W4 in lo
	public List<Integer> W5 = new ArrayList<Integer>();
	//record the corresponding attribute of W4 in tk
	public List<Integer> W6 = new ArrayList<Integer>();
	
	
	public List<String> anti_attrs = new ArrayList<String>();
	public List<Integer> lo =  new ArrayList<Integer>();
	public List<String> attr =  new ArrayList<String>();
	public String ThresholdAT; 
	
	//create a matrix M=(1), where row=1 and column=1
	private void M_ini() {
		M.add(new ArrayList<Integer>());
		M.get(0).add(1);
	}
	
	//Reference Paper: Efficient Generation of Linear Secret Sharing Scheme Matrices from Threshold Access Trees(2010)
	public void LSSS(String s)
	{
		System.out.println("---generate LSSS Matrix---");
		row=1;
		column=1;
		int tempi,tempi2,tempi3,i;
		int child_num,threshold;
		List<String> tempL= new ArrayList<String>();
		List<Integer> tempL2= new ArrayList<Integer>();
		ThresholdAT=s;
		attr.add(s);
		M_ini();
		
		i=0;
		while(i<attr.size()){
			//printfList(attr);
			//printfMatrix_M();
			if(WheatherTheThresholdString(attr.get(i))) {
				tempL=SplitString(attr.get(i));
				child_num=tempL.size()-1;
				try {
					threshold=Integer.parseInt( tempL.get(tempL.size()-1) )-1;
				}catch (NumberFormatException e) {
					System.out.println(tempL.get(tempL.size()-1));
					break;
			    }
				attr.remove(i);
				//add the element of split string tempL to list L
				tempi=tempL.size()-1;
				for(tempi2=0;tempi2<tempi;tempi2++) 
					attr.add(i+tempi2,tempL.get(tempi2));
				//handle the matrix
				tempi3=1;
				for(tempi=0;tempi<M.size();tempi++) {
					//when the index is the child-node term
					if(tempi==i) {
						//copy the list M[i]
						//attention that the wrong copy method would cause pass by reference
						//https://stackoverflow.com/questions/14319732/how-to-copy-a-java-util-list-into-another-java-util-list
						tempL2.clear();
						tempL2 = new ArrayList<>(M.get(i));
						
						M.add(tempi,new ArrayList<>(tempL2));
						tempi3=1;
						tempi2=threshold;
						M.get(tempi).add((int) Math.pow(tempi3,tempi2));
					}else if(tempi>i&&tempi<i+child_num-1) {
						M.add(tempi,new ArrayList<>(tempL2));
						tempi2=threshold;
						M.get(tempi).add((int) Math.pow(tempi3,tempi2));
					}else if(tempi==i+child_num-1) {
						tempi2=threshold;
						M.get(tempi).add((int) Math.pow(tempi3,tempi2));
					}else {
						//fill 0
						M.get(tempi).add(0);
					}
					tempi3++;
				}
			}else
				i++;
		}
		row=M.size();
		column=M.get(0).size();

		System.out.println(s);
		System.out.println("---Printf Attr ---");
		printfList(attr);
		printfMatrix_M();
		generate_lo();
	}

	public Boolean check_attr(String s, ArrayList<String> CT_attr ){
		List<String> temp= new ArrayList<String>();
		int i,meet_attr_num,threshold;
		meet_attr_num=0;
		//if the string is not a attribute but a threshold string
		if (WheatherTheThresholdString(s)) {
			//split the threshold string
			temp.addAll(SplitString(s));
			try {
				threshold=Integer.parseInt( temp.get(temp.size()-1) );
			}catch (NumberFormatException e) {
				return false;
		    }
			temp.remove(temp.size()-1);
			
			for(i=0;i<temp.size();i++) {
				if(check_attr(temp.get(i),CT_attr))
					meet_attr_num++;
				if(meet_attr_num==threshold)
					return true;
			}
		}else {
			for(i=0;i<CT_attr.size();i++) {
				if(s.equals(CT_attr.get(i)))
					return true;
			}
		}
		return false;
	}
	
	//greatest common divisor
	public static int GCD(int m, int n) { 
	    int temp = 0;
	    while(n != 0) { 
	    	temp = m % n; 
	        m = n; 
	        n = temp; 
	    }
	    return Math.abs(m); 
	}
	
	//initialize the anti-matrix omega W2 of W
	public void W2_ini(Pub PK) {
		int i,j;
		for(i=0;i<W.size();i++) {
			W2.add(new ArrayList<Element>());
			for(j=0;j<W.size();j++){
				if(i==j)
					W2.get(i).add(PK.p.getZr().newOneElement());
				else
					W2.get(i).add(PK.p.getZr().newZeroElement());
			}
		}
		Element temp=PK.p.getZr().newRandomElement();
	}
	
	//https://en.wikipedia.org/wiki/Gaussian_elimination
	public void W2_Gaussian() {
		//iterator
		int i,j,n,m,position;
		//record the numerator and denominator
		int imul,imul2;
		ArrayList<Integer> TempIntArry=new ArrayList<Integer>();
		ArrayList<Element> TempEleArry=new ArrayList<Element>();
		
		//initialize
		j=0;
		//convert matrix W to:
		// 1 a b c ...
		// 0 d e f ...
		// 0 g h i ...
		// 0 . . . ...
		for(i=1;i<W.size();i++) {
			//each row, while the row is not in the end. Remark that not have to handle the first row in the first column position.
			if(i<W.size()-1) {
				//handle W
				for(j=0;j<W.get(0).size();j++) {
					W.get(i).set(j,W.get(i).get(j)-W.get(i+1).get(j));
				}
				//handle W2
				for(j=0;j<W2.get(0).size();j++) {
					//System.out.println(j+" "+W2.get(i+1).get(j).duplicate().mul(BigInteger.valueOf(-1)));
					//System.out.println(j+" "+W2.get(i+1).get(j).duplicate().getImmutable());
					
					W2.get(i).get(j).add(W2.get(i+1).get(j).duplicate().mul(BigInteger.valueOf(-1)));
					
					if(!W2.get(i).get(j).isZero()) {
						
						System.out.println("***********************************");
						System.out.println(j+" "+W2.get(i).get(j).duplicate());
						System.out.println(j+" "+W2.get(i).get(j).duplicate().invert());
						System.out.println(j+" "+W2.get(i).get(j).duplicate().mul(BigInteger.valueOf(-1)));
					}
					
				}
			}else {
				//handle W
				for(j=0;j<W.get(0).size();j++) {
					W.get(i).set(j,W.get(i).get(j)-W.get(0).get(j));
				}
				//handle W2
				for(j=0;j<W2.get(0).size();j++) {
					W2.get(i).get(j).add(W2.get(0).get(j).duplicate().mul(BigInteger.valueOf(-1)));
				}
			}
		}
		position=1;
		//convert matrix W to:
		// 1 0 0 0 ...
		// 0 0 0 0 ...
		// 0 0 0 0 ...
		// 0 . . . ...
		//each column
		while(position<W.get(0).size()) {
			//each row
			for(i=0;i<W.size();i++) {
				
				//initialize
				TempIntArry.clear();
				TempEleArry.clear();
				
				for(m=1;m<W.size();m++) {
					if(W.get(m).get(position)!=0)
						break;
				}
				if(m<W.size()) {
					TempIntArry.addAll(W.get(m));
					TempEleArry.addAll(W2.get(m));
					
					//while the row is not in the end and the coefficient!=0
					if(i<W.size()&&W.get(i).get(position)!=0) {
						/*calculate coefficient imul and imul2*/
						//confirm not divide by 0
						for(n=1;n<W.size()-1;n++) {
							if(W.get((i+n)%W.size()).get(position)!=0)
								break;
						}
						if(i+n>=W.size()) {
							imul=GCD(W.get(i).get(position),TempIntArry.get(position));
							imul2=imul;
							imul=TempIntArry.get(position)/imul;
							imul2=W.get(i).get(position)/imul2;
							imul2*=-1;
							
							//handle W
							for(j=0;j<W.get(0).size();j++) {
								W.get(i).set(j,W.get(i).get(j)*imul+TempIntArry.get(j)*imul2);
							}
							//handle W2
							for(j=0;j<W2.get(0).size();j++) {
								W2.get(i).get(j).mul(BigInteger.valueOf(imul));
								W2.get(i).get(j).add(TempEleArry.get(j).duplicate().mul(BigInteger.valueOf(imul2)));
							}
						}else {
							imul=GCD(W.get(i).get(position),W.get(i+n).get(position));
							imul2=imul;
							imul=W.get(i+n).get(position)/imul;
							imul2=W.get(i).get(position)/imul2;
							imul2*=-1;
							
							//handle W
							for(j=0;j<W.get(0).size();j++) {
								W.get(i).set(j,W.get(i).get(j)*imul+W.get(i+n).get(j)*imul2);
							}
							//handle W2
							for(j=0;j<W2.get(0).size();j++) {
								W2.get(i).get(j).mul(BigInteger.valueOf(imul));
								W2.get(i).get(j).add(W2.get(i+n).get(j).duplicate().mul(BigInteger.valueOf(imul2)));
							}
						}
					}
				}
			}
			position++;
		}
	}
	
	
	public void getFirstW2(Pub PK) {
		int i;
		Element e;
		e=PK.p.getZr().newElement().set(W.get(0).get(0)).invert();
		for(i=0;i<W2.get(0).size();i++) {
			W3.add(W2.get(0).get(i).duplicate().mul(e));
		}
	}
	
	public void getW4W5W6() {
		int i,j;
		//find the attr is satisfied
		for(i=0;i<lo.size();i++) {
			for(j=0;j<anti_attrs.size();j++) {
				if(attr.get(lo.get(i)).equalsIgnoreCase(anti_attrs.get(j))) {
					W4.add(W3.get(j).duplicate());
					W5.add(i);
					W6.add(j);
					break;
				}
			}
		}
	}

	//print the matrix W6 of the class
	public void printfMatrix_W6()
	{
		row=W6.size();
		System.out.println("---Printf Matrix W6---");
		System.out.println("row    = "+W6.size());
		int i;
		for(i=0;i<W6.size();i++) {
			System.out.println(W6.get(i));
		}
		System.out.println("------");
	}
	
	
	//print the matrix W5 of the class
	public void printfMatrix_W5()
	{
		row=W5.size();
		System.out.println("---Printf Matrix W5---");
		System.out.println("row    = "+W5.size());
		int i;
		for(i=0;i<W5.size();i++) {
			System.out.println(W5.get(i));
		}
		System.out.println("------");
	}
	
	//print the matrix W4 of the class
	public void printfMatrix_W4()
	{
		row=W4.size();
		System.out.println("---Printf Matrix W4---");
		System.out.println("row    = "+W4.size());
		int i;
		for(i=0;i<W4.size();i++) {
			System.out.println(W4.get(i));
		}
		System.out.println("------");
	}
	
	//print the matrix W3 of the class
	public void printfMatrix_W3()
	{
		row=W3.size();
		System.out.println("---Printf Matrix W3---");
		System.out.println("row    = "+W3.size());
		int i;
		for(i=0;i<W3.size();i++) {
			System.out.println(W3.get(i));
		}
		System.out.println("------");
	}
	
	//print the matrix W2 of the class
	public void printfMatrix_W2()
	{
		row=W2.size();
		System.out.println("---Printf Matrix W2---");
		System.out.println("row    = "+W2.size());
		int i;
		for(i=0;i<W2.size();i++) {
			System.out.println(W2.get(i));
		}
		System.out.println("------");
	}
	
	//create a matrix W=(1), where row=1 and column=1
	public void W_ini() {
		W.add(new ArrayList<Integer>());
		W.get(0).add(1);
	}
	
	//transfer string to matrix omega W, x record the deep of matrix W (s,x)(s is not important)
	//y record the number of parent 
	public void find_anti(String s,ArrayList<String> CT_attr,int x,int y){
		List<String> temp= new ArrayList<String>();
		List<Integer> temp2 = new ArrayList<Integer>();
		int i,j,n = 0, layer;
		boolean attr_belong=false;
		if (WheatherTheThresholdString(s)) {
			temp.addAll(SplitString(s));
			
			n=Integer.parseInt(temp.get(temp.size()-1));
			temp.remove(temp.size()-1);
			j=0;
			//construct this layer of matrix W
			for(i=0;i<W.size();i++)
			{
				if(W.get(i).get(x)==y) {
					j++;
					temp2.clear();
					temp2 = new ArrayList<>(W.get(i));
					W.remove(i);
					for(;j<=temp.size();j++) {
						W.add(i,new ArrayList<>(temp2));
						W.get(i).add((int)Math.pow(j,n-1));
						i++;
					}
					i--;
				}else {
					W.get(i).add(0);
				}
				if(i>=W.size())
					break;
			}
			//check line in Matrix W have permission and do recursive
			i=0;
			layer=W.get(0).size()-1;
			for(i=0;i<temp.size();i++)
			{
				if(check_attr(temp.get(i), CT_attr)) {
					find_anti(temp.get(i), CT_attr,layer,i+1);
				}else {
					//Remove the line in Matrix W without permission
					for(j=0;j<W.size();j++)
					{
						if(W.get(j).get(x)==y) {
							if(W.get(j).get(layer)==i+1) {
								W.remove(j);
								break;
							}
						}
					}
				}
			}
		}else{
			for(i=0;i<CT_attr.size();i++) {
				if(s.equals(CT_attr.get(i))) {
					attr_belong=true;
					break;
				}
			}
			if(!attr_belong)
				W.remove(x);
			else
				anti_attrs.add(CT_attr.get(i));
		}
	}
	
	//print the matrix W of the class
	public void printfMatrix_W()
	{
		row=W.size();
		column=W.get(0).size();
		System.out.println("---Printf Matrix W---");
		System.out.println("row    = "+W.size()+"\ncolumn = "+W.get(0).size());
		int i,j;
		for(i=0;i<W.size();i++) {
			System.out.printf("%-4d",i);
			for(j=0;j<W.get(i).size();j++)
				System.out.printf("%-3d",W.get(i).get(j));
			System.out.println("");
		}
		System.out.println("------");
	}
	
	//random the matrix M index and use the list lo to record the original index
	private void generate_lo() {
		Random ran = new Random();
		int i,rnum;
		//initialize List lo
		for(i=0;i<M.size();i++) 
			lo.add(i);
		for(i=0;i<M.size();i++) {
			rnum=ran.nextInt(M.size());
			Collections.swap(M, i, rnum);
			Collections.swap(lo, i, rnum);
		}
		System.out.println("---Printf List lo---");
		printfList_int(lo);
		printfMatrix_M();
	}
	//print the list the function get
	public void printfList (List<String> L)
	{
		int i;
		for(i=0;i<L.size();i++) {
			System.out.println(i+1+"  "+L.get(i));
		}
	}
	//print the list the function get
	public void printfList_int (List<Integer> L)
	{
		int i;
		for(i=0;i<L.size();i++) {
			System.out.println(i+1+"  "+L.get(i));
		}
	}
	//print the matrix M of the class
	public void printfMatrix_M()
	{
		row=M.size();
		column=M.get(0).size();
		System.out.println("---Printf Matrix M---");
		System.out.println("row    = "+row+"\ncolumn = "+column);
		int i,j;
		for(i=0;i<M.size();i++) {
			System.out.printf("%-4d",i);
			for(j=0;j<M.get(i).size();j++)
				System.out.printf("%-3d",M.get(i).get(j));
			System.out.println("");
		}
		System.out.println("------");
	}
	
	private boolean WheatherTheThresholdString(String s)
	{
		Stack<Integer> numcheck = new Stack<Integer>();
		if(s.charAt(0)!='(')
			return false;
		numcheck.push(0);
		int i=0,l=1,r=0;
		int temp;
		while(i<s.length()-1) {
			i++;
			if(s.charAt(i)=='('){
				l++;
				numcheck.push(0);
			}else if(s.charAt(i)==')') {
				r++;
				if(l>r)
					continue;
				//check the number of the "(" is bigger than")"
				if(l<r)
					return false;
				//if the number of "(" is equal to ")" but string is not in the end
				else if(l==r&&i!=s.length()-1)
					return false;
				//the threshold value cannot bigger than the number of element
				else {
					try {
						temp=Integer.parseInt(s.substring(s.lastIndexOf(',',i)+1, i));
						if(temp>numcheck.pop())
							return false;
					}catch (NumberFormatException e) {
						System.out.println(s.substring(s.lastIndexOf(',',i)+1, i));
						return false;
				    }
				}
			}else if(s.charAt(i)==',') {
				temp=numcheck.peek()+1;
				numcheck.pop();
				numcheck.push(temp);
			}
		}
		return true;
	}
	
	private List<String> SplitString(String s){
		int i=1,l=0,r=0,pre=1;
		List<String> str = new ArrayList<String>();
		while(i<s.length()-1) {
			if(s.charAt(i)==','&&l==r) {
				str.add(s.substring(pre,i));
				pre=i+1;
			}
			else if(s.charAt(i)=='('){
				l++;
			}
			else if(s.charAt(i)==')'){
				r++;
			}
			i++;
		}
		str.add(s.substring(pre,i));
		return str;
	}
	
}
