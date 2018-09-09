package algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import format.*;
import it.unisa.dia.gas.jpbc.Element;

public class encrypt {
	/*
	 * the encryption algorithm run by the device in the IoT environment.
	 * first it run the function: public enc_device(Pub pub, String M, String attr).
	 * it would outsourcing some exponentiation calculation to assistant nodes (call the enc_assistant).
	 * and after get the return value from assistant nodes, it run the function: enc_device(int s).
	 * the function would send the partial ciphertext to server ( call the enc_server ).
	 */
	
	public Pub PK =new Pub();
	public Cph cph=new Cph();
	public Cph direct_cph=new Cph();
	public LsssAccessStructure AT=new LsssAccessStructure();
	public Element Message;
	
	
	
	//the list record the outsource order s, lambda and r, where index is 0, 1 and 2.
	List<List<Integer>> List =new ArrayList<List<Integer>>();
	//used in the split func
	List<Integer>ReturnList_L=new ArrayList<Integer>();
	
	int NumOfAssistantNode;
	//used after the assistant nodes return the computation result
	int D_RecievePackage=0;
	int S_RecievePackage=0;
	//original share which is after Split
	List<Element> s =new ArrayList<Element>();
	List<Element> r= new ArrayList<Element>();
	List<Element> lambda= new ArrayList<Element>();
	List<Element> Func= new ArrayList<Element>();
	//the message which is going to be encrypt
	List<Element> v= new ArrayList<Element>();
	
	//collect the return from assistant node 
	List<Enc_AssistantRetrunToDevice> receive_D =new ArrayList<Enc_AssistantRetrunToDevice>();
	List<Enc_AssistantRetrunToServer> receive_S= new ArrayList<Enc_AssistantRetrunToServer>();
	//if exist any assistant node cheat the device, then stop the encryption algorithm
	boolean check=true;
	
	/*
	 * encryption algorithm of the device phase.
	 * input : public parameters PK and message M.
	 * output: outsourcing computation to assistant node
	 */
	public void enc_device(Pub pub, String M, String attr){
		System.out.println("-------------------encryption----------------------");
		int i,j;
		PK=pub;
		Message = PK.p.getGT().newRandomElement();
		AT.LSSS(attr);
		Element temp =PK.p.getZr().newElement();
		Random ran = new Random();
		
		cph.ori_msg=Message.duplicate();
		System.out.println("---generate random vector v---");
		v.add(PK.p.getZr().newElement().setToRandom());
		System.out.println("1  "+v.get(0));
		for(i=1;i<AT.column;i++) {
			v.add(v.get(0).duplicate().pow(BigInteger.valueOf(i+1)));
			System.out.println(i+1+"  "+v.get(i));
		}
		
		System.out.println("---generate vector lambda---");
		for(i=0;i<AT.row;i++) {
			temp.setToZero();
			for(j=0;j<AT.column;j++) {
				temp.add(v.get(j).duplicate().mul(BigInteger.valueOf(AT.M.get(i).get(j))));
			}
			lambda.add(temp.duplicate());
			System.out.println("lambda["+i+"]= "+lambda.get(i));
		}
		
		System.out.println("---generate random vector r---");
		for(i=0;i<AT.row;i++) {
			r.add(PK.p.getZr().newElement().setToRandom());
			System.out.println("r["+i+"]= "+r.get(i));
		}
		
		//------generate the direct cph-------//
		/*
		Element test =PK.p.getG1().newElement().setToRandom();
		System.out.println(test);
		System.out.println(test.duplicate().powZn(v.get(0).duplicate()));
		*/
		System.out.println("-----------compute direct cph use for final verification---------------------");
		direct_cph.c= Message.duplicate().mul(PK.g_hat_alpha.duplicate().powZn(v.get(0).duplicate()));
		direct_cph.g_s= PK.g.duplicate().powZn(v.get(0).duplicate());
		Element e=PK.p.getG1().newElement().setToOne();
		
		for(i=0;i<r.size();i++) {
			//direct_cph.Comps.add(new CphComps());
			direct_cph.Ci.add(PK.g_a.duplicate().powZn(lambda.get(i).duplicate()) .mul(  PK.H(AT.attr.get(AT.lo.get(i))).powZn( r.get(i).duplicate() ).invert()));
			//System.out.println(i+":"+PK.g_a.duplicate().powZn(lambda.get(i).duplicate()).mul(e.duplicate().div(PK.H(AT.attr.get(AT.lo.get(i))).powZn(r.get(i).duplicate())) ));
			direct_cph.Di.add( PK.g.duplicate().powZn(r.get(i).duplicate()));
		}
		//------generate the direct cph ------end//
		
		//used in test
		cph.test_s=v.get(0).duplicate();
		cph.test_lambda.addAll(lambda);
		//---------used in test end
		
		
		//we assume that the number of assistant node is 2~12
		NumOfAssistantNode=ran.nextInt(10)+2;
		
		System.out.println("---Detect there have "+NumOfAssistantNode+" assistant node can help outsourcing---");

		System.out.println("---Split the secrect encryption exponent s---");
		s.add(v.get(0).duplicate());
		s=Split(s,5,1);
		List.add(new ArrayList<Integer>());
		List.get(0).addAll(ReturnList_L);

		System.out.println("---Split the vector lambda---");
		System.out.println("lambda size:"+lambda.size());
		lambda=Split(lambda,(int)Math.ceil(lambda.size()*PK.SecurityDegree),1);
		List.add(new ArrayList<Integer>());
		List.get(1).addAll(ReturnList_L);
		
		System.out.println("---Split the vector r---");
		System.out.println("r size:"+r.size());
		r=Split(r,(int)Math.ceil(lambda.size()*PK.SecurityDegree),1);
		List.add(new ArrayList<Integer>());
		List.get(2).addAll(ReturnList_L);
		
		System.out.println("---distribute the splited data s,lambda and r into "+NumOfAssistantNode+" package---");
		PickOutEnc_DeviceOutsourceToAssistantNode();
	}

	//diff_num mean that there have diff_num*q.size() different element between them.
	//split_num mean the size of output set S, the size of output set S may slightly bigger than split_num.
	private List<Element> Split(List<Element> q, int split_num, int diff_num) {
		List<Element>rand=new ArrayList<Element>();
		List<Element>base=new ArrayList<Element>();
		List<Element>diff=new ArrayList<Element>();
		//the return universe
		List<Element>S=new ArrayList<Element>();
		Random ran = new Random();
		Element BaseSum,TempSum;
		int i,j,n,rnum;
		//initialize
		ReturnList_L.clear();
		//check reasonable of the split_num
		if(split_num<q.size()*1) {
			split_num=(int) (q.size()*1);
		}
		//create random set (rand) and based set (base).
		n=(int)Math.ceil((double)(split_num-q.size()*diff_num)/2);
		BaseSum=PK.p.getZr().newElement().setToZero();
		for(i=0;i<n;i++) {
			rand.add(PK.p.getZr().newElement().setToRandom());
			base.add(PK.p.getZr().newElement().setToRandom());
			//calculate the sum of the based set
			BaseSum.add(base.get(i));
		}
		//create the different set diff
		n=q.size();
		for(i=0;i<n;i++) {
			TempSum=BaseSum.duplicate().setToZero();
			for(j=1;j<=diff_num;j++){
				if(j<diff_num){
					diff.add(PK.p.getZr().newElement().setToRandom());
					TempSum.add(diff.get(diff.size()-1).duplicate());
				}else {
					diff.add(q.get(i).duplicate().sub(TempSum.duplicate().add(BaseSum.duplicate())));
				}
			}
		}
		
		//collect all the set rand, base and diff; then generate a universe set S
		S.addAll(rand);	//record value = -1
		S.addAll(base);	//record value = 0
		S.addAll(diff);	//record value = 1 ~ diff.size()
		
		//initialize the record list ReturnList_L
		for(i=0;i<rand.size();i++)
			ReturnList_L.add(-1);
		for(i=0;i<base.size();i++)
			ReturnList_L.add(0);
		for(i=0;i<diff.size();i++)
			for(j=1;j<=diff_num;j++)
				ReturnList_L.add(i+1);
		
		//random the index of the set S 
		for(i=0;i<S.size();i++) {
			rnum=ran.nextInt(S.size());
			Collections.swap(S, i, rnum);
			Collections.swap(ReturnList_L, i, rnum);
		}
		//output the index,record list and the value
		for(i=0;i<S.size();i++) {
			System.out.printf("%-3d",i);
			System.out.printf("%3d",ReturnList_L.get(i));
			System.out.println(" "+S.get(i));
		}
		return S;
	}
	
	//device outsource the computation to assistant nodes one by one
	private void PickOutEnc_DeviceOutsourceToAssistantNode() {
		//all outsourcing data set
		List<Enc_DeviceOutsourceToAssistantNode> output = new ArrayList<Enc_DeviceOutsourceToAssistantNode>();
		//the (temperature) set of shares that split from set to assistant nodes. 
		List<List<Element>> Q = new ArrayList<List<Element>>();
		int i,j;
		for(i=0;i<NumOfAssistantNode;i++) {
			output.add(new Enc_DeviceOutsourceToAssistantNode());
			output.get(i).ID=i;
		}
		System.out.println("------PickOut s and create the F(lo(i))-------");
		Q=PickOut(s,NumOfAssistantNode);
		j=NumOfAssistantNode;
		//System.out.println("***"+j+" "+output.size());
		for(i=0;i<j;i++) {
			output.get(i).s.addAll(Q.get(i));
		}
		
		j=AT.lo.size();
		for(i=0;i<j;i++) {
			Func.add(PK.H(AT.attr.get(AT.lo.get(i))));
		}
		System.out.println("------PickOut r-------");
		Q.clear();
		Q=PickOut(r,NumOfAssistantNode);
		for(i=0;i<NumOfAssistantNode;i++)
			output.get(i).r.addAll(Q.get(i));
		System.out.println("------PickOut lambda-------");
		Q.clear();
		Q=PickOut(lambda,NumOfAssistantNode);
		
		for(i=0;i<NumOfAssistantNode;i++) {
			output.get(i).lambda.addAll(Q.get(i));
			output.get(i).attr_H.addAll(Func);
			
			//send the outsouce computation and the ID to Assistant Nodes
			enc_assistant(output.get(i),i+1);
		}
	}
	
	//let set set split to a bigger set, include random element, based element and different element
	private List<List<Element>> PickOut(List<Element> S,int p) {
		List<List<Element>> Q = new ArrayList<List<Element>>();
		//compute the size of the subset in roughly
		int sub_num=(int)Math.ceil((double)S.size()*PK.SecurityDegree/p);
		int i,j,interval,position,turn;	
		//generate the subset Q
		position=0;
		interval=1;
		turn=0;
		for(i=0;i<p;i++){
			Q.add(new ArrayList<Element>());
			for(j=0;j<sub_num;j++) {
				Q.get(i).add(S.get(position).duplicate());
				position+=interval;
				if(position>=S.size()) {
					turn++;
					if(turn==interval) {
						interval++;
						turn=0;
						position=0;
					}else{
						position=turn;
					}
				}
			}
		}
		
		//Printf the PickOut condition
		System.out.println("------Printf the PickOut condition-------");
		System.out.println("share = "+p);
		System.out.println("share data size = "+sub_num);
		System.out.println("Original data size = "+S.size());
		List<List<Integer>> condition = new ArrayList<List<Integer>>();
		position=0;
		interval=1;
		turn=0;
		//printf the pickout condition in number
		for(i=0;i<p;i++) {
			condition.add(new ArrayList<Integer>());
			for(j=0;j<sub_num;j++) {
				condition.get(i).add(position);
				position+=interval;
				if(position>=S.size()) {
					turn++;
					if(turn==interval) {
						interval++;
						turn=0;
						position=0;
					}else{
						position=turn;
					}
				}
			}
		}
		//printf the pickout condition in chart
		System.out.printf("p\\s ");
		for(j=0;j<S.size();j++) {
			System.out.printf("%-4d",j+1);
		}
		System.out.println();
		for(i=0;i<p;i++) {
			Collections.sort(condition.get(i));
			System.out.printf("%-4d",i+1);
			position=0;
			for(j=0;j<S.size();j++) {
				if(j==condition.get(i).get(position)) {
					position++;
					System.out.printf("o   ");
					if(position==condition.get(i).size())
						break;
				}else
					System.out.printf("    ");
			}System.out.println();
		}
		//Printf the PickOut condition----------end
		
		return Q;
	}
	
	/*
	 * encryption algorithm of the assistant node phase
	 * input : the outsourcing formula from the device
	 * output: the answer of the formula
	 */
	private void enc_assistant(Enc_DeviceOutsourceToAssistantNode computation,int num) {
		int i,j,n,m;
		Enc_AssistantRetrunToDevice todevice=new Enc_AssistantRetrunToDevice();
		Enc_AssistantRetrunToServer toserver=new Enc_AssistantRetrunToServer();
		
		System.out.println("-------------------encryption of the assistant node phase: node "+num+"----------------------");
		todevice.ID=num;
		toserver.ID=num;
		n=computation.s.size();
		for(i=0;i<n;i++) {
			//compute ( e(g,g)^alpha )^s(i)
			todevice.g_hat_alpha.add(PK.g_hat_alpha.duplicate().powZn(computation.s.get(i).duplicate()));
			//compute g^s(i)
			toserver.g_s.add(PK.g.duplicate().powZn(computation.s.get(i).duplicate()));
		}
		
		Element e;
		e = PK.p.getZr().newRandomElement();
		e.setToOne();
		n=computation.lambda.size();
		for(i=0;i<n;i++) {
			//compute ( g^a )^lambda(i)
			toserver.g_aMulLambda.add(PK.g_a.duplicate().powZn(computation.lambda.get(i).duplicate()));
		}
		
		n=computation.r.size();
		m=computation.attr_H.size();
		//initialize func_r array
		for(j=0;j<m;j++) {
			toserver.func_r.add(new ArrayList<Element>());
		}
		
		for(i=0;i<n;i++) {
			//compute g^r(i)
			toserver.g_r.add(PK.g.duplicate().powZn(computation.r.get(i).duplicate() ));
			//compute func^r
			for(j=0;j<m;j++) {
				toserver.func_r.get(j).add(computation.attr_H.get(j).duplicate().powZn( computation.r.get(i).duplicate() ));
			}
		}
		
		//return the computation result to device and server
		RecievePackage_Server(toserver);
		RecievePackage_Device(todevice);
	}
	
	//receive the data from the assistant node in the device phase, check the number of receive package is correct
	public void RecievePackage_Device(Enc_AssistantRetrunToDevice data) {
		D_RecievePackage++;
		System.out.println("-------------------encryption of the Device receive "+D_RecievePackage+" Package from assistant nodes----------------------");
		receive_D.add(data);
		//if receive all computation result
		if(D_RecievePackage==NumOfAssistantNode) {
			//initialize D_RecievePackag, prepare for the condition that the device want to encrypt multiply message
			D_RecievePackage=0;
			//Assume that the receive result is not in order, because the traffic in the IoT. After receive all computation result, the device sort first
			//But actually, the receive result is in order because we bypass the factor.
			Comparator<Enc_AssistantRetrunToDevice> comp = (Enc_AssistantRetrunToDevice a, Enc_AssistantRetrunToDevice b) -> {
			    return a.ID-b.ID;
			};
			Collections.sort(receive_D, comp);
			enc_device();
		}
	}
	
	//receive the data from the assistant node in the server phase, check the number of receive package is correct
	public void RecievePackage_Server(Enc_AssistantRetrunToServer data) {
		S_RecievePackage++;
		System.out.println("-------------------encryption of the Server receive "+S_RecievePackage+" Package from assistant nodes----------------------");
		receive_S.add(data);
		//if receive all computation result
		if(S_RecievePackage==NumOfAssistantNode) {
			//Assume that the receive result is not in order, because the traffic in the IoT. After receive all computation result, the device sort first
			//But actually, the receive result is in order because we bypass the factor.
			Comparator<Enc_AssistantRetrunToServer> comp = (Enc_AssistantRetrunToServer a, Enc_AssistantRetrunToServer b) -> {
			    return a.ID-b.ID;
			};
			Collections.sort(receive_S, comp);
		}
	}
	
	/*
	 * encryption algorithm of the device phase
	 * input : Enc_AssistantRetrunToDevice
	 * output: partial ciphertext
	 */
	public void enc_device(){
		System.out.println("-------------------encryption of the device phase----------------------");
		Element CT;
		
		int i;
		List<List<Element>> set = new ArrayList<List<Element>>();
		List<Element> answer_s = new ArrayList<Element>();
		//collect the computation result of g_hat_alpha
		for(i=0;i<receive_D.size();i++) {
			set.add(new ArrayList<Element>());
			set.get(i).addAll(receive_D.get(i).g_hat_alpha);
		}
		
		//check the return result is correct
		System.out.println("-------------------compute s----------------------");
		answer_s=CheckResult(set, List.get(0));
		
		//because if you want to use the mul function that both of element have to be GT format. but after use the newElementFromBytes function, in other word, is meaning to hash the message than it is impossible to get the original message in the decrypt phase. so we assume that the message M can mapping to the element.
		CT = Message.duplicate().mul(answer_s.get(0).duplicate());
		System.out.printf("answer_s= "+answer_s);
		System.out.printf("CT= "+CT);
		
		enc_server(CT);
	}
	
	private List<Element> CheckResult(List<List<Element>> S, List<Integer> list) {
		List<Element> output = new ArrayList<Element>();
		List<Element> compare = new ArrayList<Element>();
		List<List<Integer>> condition = new ArrayList<List<Integer>>();
		int i,j,p,q,position,interval,turn,sub_num,size;
		Element BaseSum;
		
		System.out.println("-------------------check the computation result----------------------");
		position=0;
		interval=1;
		turn=0;
		size=list.size();
		//the pickout condition in number,record in the list condition
		p=S.size();
		sub_num=S.get(0).size();
		for(i=0;i<p;i++) {
			condition.add(new ArrayList<Integer>());
			System.out.printf("%-4d",i+1);
			for(j=0;j<sub_num;j++) {
				condition.get(i).add(position);
				System.out.printf("%-3d",position+1);
				position+=interval;
				if(position>=size) {
					turn++;
					if(turn==interval) {
						interval++;
						turn=0;
						position=0;
					}else{
						position=turn;
					}
				}
			}System.out.println();
		}
		p=condition.size();
		q=condition.get(0).size();
		position=0;
		for(i=0;i<p;i++) {
			for(j=0;j<q;j++) {
				if(position<0) {
					if(! compare.get(condition.get(i).get(j)).isEqual(S.get(i).get(j))) {
						System.out.println("there are someone cheat me(device) and i decide to stop working encryption algorithm!!!");
						check=false;
						return output;
					}
				}else if(condition.get(i).get(j)==position) {
					compare.add(S.get(i).get(j).duplicate());
					position++;
				}else {
					position=-1;
					if(! compare.get( condition.get(i).get(j) ) .isEqual( S.get(i).get(j) )) {
						System.out.println("there are someone cheat me(device) and i decide to stop working encryption algorithm!!!");
						check=false;
						return output;
					}
				}
			}
		}
		System.out.println("-------------------compute the answer----------------------");
		BaseSum=S.get(0).get(0).setToOne();
		
		//find the maximum number in the record list to get the size of output
		p=Collections.max(list);
		for(i=0;i<p;i++) {
			output.add(BaseSum.duplicate());
		}
		//compute the sum of base and the different
		p=list.size();
		for(i=0;i<p;i++) {
			if(list.get(i)==0) {
				BaseSum.mul(compare.get(i).duplicate());
			}else if(list.get(i)>0)
			{
				output.get(list.get(i)-1).mul(compare.get(i).duplicate());
			}
		}
		//add the sum of base to complete the computation
		p=output.size();
		for(i=0;i<p;i++) {
			output.get(i).mul(BaseSum.duplicate());
		}
		return output;
	}
	
	/*
	 * encrypt algorithm of the server phase
	 * input : Enc_AssistantRetrunToServer
	 * output: cph
	 */
	public void enc_server(Element CT) {
		System.out.println("-------------------encryption of the Server receive Package from Device----------------------");
		System.out.println("-------------------encryption of the server phase----------------------");
		if(S_RecievePackage!=NumOfAssistantNode) {
			System.out.println("error: didn't receive correct number of package("+S_RecievePackage+") from assistant node("+NumOfAssistantNode+").");
			//initialize S_RecievePackage, prepare for the condition that the device want to encrypt multiply message
			S_RecievePackage=0;
		}
		else {
			List<List<Element>> temp= new ArrayList<List<Element>>();
			List<List<List<Element>>> temp2= new ArrayList<List<List<Element>>>();
			List<Element> ans = new ArrayList<Element>();
			int i,j,n,m;
			
			cph.c=CT.duplicate();
			cph.AT=AT;
			//compute cph.g_s
			n=receive_S.size();
			temp.clear();
			for(i=0;i<n;i++) {
				temp.add(new ArrayList<Element>());
				temp.get(i).addAll(receive_S.get(i).g_s);
			}ans=CheckResult(temp,List.get(0));
			cph.g_s=ans.get(0).duplicate();
			
			/*compute cph.Comps*/
			//initialize temp and ans List
			temp.clear();
			ans.clear();
			//compute g_alphaMulLambda
			for(i=0;i<n;i++) {
				temp.add(new ArrayList<Element>());
				temp.get(i).addAll(receive_S.get(i).g_aMulLambda);
			}ans=CheckResult(temp,List.get(1));
			//write g_alphaMulLambda to cph
			m=ans.size();
			for(i=0;i<m;i++) {
				cph.Ci.add(ans.get(i).duplicate());
			}
			
			//compute F(lo(i))^-r(i)
			Element e=PK.p.getG1().newElement().setToOne();
			
			m=receive_S.get(0).func_r.size();
			for(i=0;i<m;i++) {
				temp2.add(new ArrayList<List<Element>>());
				for(j=0;j<n;j++) {
					temp2.get(i).add(new ArrayList<Element>());
					temp2.get(i).get(j).addAll(receive_S.get(j).func_r.get(i));
				}
			}
			
			for(j=0;j<m;j++) {
				//initialize ans List
				ans.clear();
				ans=CheckResult(temp2.get(j),List.get(2));
				cph.Ci.get(j).mul(e.duplicate().div(ans.get(j).duplicate()));
			}
			
			//initialize temp and ans List
			temp.clear();
			ans.clear();
			//compute D(i) = g ^ r(i)
			for(i=0;i<n;i++) {
				temp.add(new ArrayList<Element>());
				temp.get(i).addAll(receive_S.get(i).g_r);
				}ans=CheckResult(temp,List.get(2));
			//write g^r(i) to cph.Comps.D
			m=ans.size();
			
			System.out.println("-----------------cph---------------------");
			System.out.println("Message:"+Message);
			System.out.println("c  :"+cph.c);
			System.out.println("c' :"+cph.g_s);
			for(i=0;i<m;i++) {
				cph.Di.add(ans.get(i).duplicate());
				System.out.println("C("+i+"):" + cph.Ci.get(i));
				System.out.println("D("+i+"):" + cph.Di.get(i));
			}
			
			System.out.println("-----------------direct cph---------------------");
			System.out.println("c  :"+direct_cph.c);
			System.out.println("c' :"+direct_cph.g_s);
			//System.out.println("g  :"+PK.g.duplicate());
			
			for(i=0;i<direct_cph.Ci.size();i++) {
				System.out.println("C("+i+"):" + direct_cph.Ci.get(i));
				System.out.println("D("+i+"):" + direct_cph.Di.get(i));
			}
			
			//test
			System.out.println("---generate random vector v---");
			for(i=0;i<AT.column;i++) {
				System.out.println(i+1+"  "+v.get(i));		
				System.out.println("lambda["+i+"]= "+lambda.get(i));
				System.out.println("r["+i+"]= "+r.get(i));
			}
			
			//---------------------------test end
			
			System.out.println("-----------------encryption end---------------------");
		}
	}
}
