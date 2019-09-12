

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class SicAssembler
{
	Map<String,String> symbTab = new HashMap<String, String>();
	Map<String,String> literalTab = new HashMap<String, String>();
	ArrayList<Operation> outputPhase1 = new ArrayList<Operation>();
	String locationCounter;
	int programLengthINT=0;
	int base;
	boolean ltorg = false; //new addition
	String lcLtorg;//new addition
	String ProgramLength ;
	boolean baseRelativeAddressing = false;
	Load loader = new Load();
	boolean start = false;
	boolean end =false;
	boolean errorExists = false;
	boolean freeFormat = false;
	private Scanner scan;
	private Operation op;
	
	public void read()
	{
		System.out.println("(1) Free Format \n(2) Fixed Format ");
		
		scan = new Scanner(System.in);
		int choice = scan.nextInt();
		if(choice==1) freeFormat = true;
		
		 try {
	 			File file = new File("file.txt");
	 			FileReader fileReader = new FileReader(file);
	 			BufferedReader bufferedReader = new BufferedReader(fileReader);		
	 			String line;
	 			 String lastLine = null;
	 			
	 			while ((line = bufferedReader.readLine()) != null) 
	 			{
	 				line = line.toLowerCase();
	 		
	 				if( line.charAt(0) == '.')
	 					continue;
	 				else
	 				Assemble(line);
	 				lastLine=line;
	 			}
	 			 checkEnd(lastLine);
                                  ProgramLength = Integer.toHexString(programLengthINT);
	 			 fixLiteralTab();
	 			 //ProgramLength = CalculateHexa( outputPhase1.get(0).address,outputPhase1.get(outputPhase1.size()-1).address);
	 			fileReader.close();
	 			;
	 		} catch (IOException e) {
	 			e.printStackTrace();
	 		} 
	}
	
	
	
	public Operation freeFormatSplit(String str)
	{
		String[] splited = str.trim().split("\\s+");
		if(splited.length==3)
		return split(str,splited[0],splited[1],splited[2]);

		return split(str," ",splited[0],splited[1]);
	}
	
	public Operation fixedFormatSplit(String s)
	{
		String label = s.substring(0,8);
		String	 opcode = s.substring(9,15);
		String	operand = s.substring(17,35);
		return split(s,label,opcode,operand);
	}
	public void literalCheck(String operand) //assume operand is sent trimed.
	{
		if( operand.length() < 5)
			return;
			char char0 = operand.charAt(0);
			char char1 = operand.charAt(1);
			char char2 = operand.charAt(2);
			char charLast = operand.charAt(operand.length()-1);
			boolean word = false;
			boolean charact = false;
			boolean hexa = false;
			int length = 0;
			if( char1 == 'w')
			{
				word = true;
				length = 3;
			}
			else if (char1 == 'c')
			{
				charact = true;
				length = operand.length() - 3;
				
			}
			else if( char1 == 'x')
			{
				hexa = true;
				length = operand.length() - 3;
			    length = length/2;
				 
			}
			if( char0 == '=' && (word || charact || hexa ) && charLast == '\'' && char2 == '\'')
				{
						literalTab.put(operand.trim(), Integer.toString(length));
				}
	}
	
	public void fixLiteralTab() {
		if( !ltorg)
		{
			lcLtorg = locationCounter;
		
		}
		
			int size = 0;
			String lastLocation = lcLtorg;
			for( Map.Entry<String, String> entry : literalTab.entrySet() )
			{
				String rep = pcPlusFormat(lastLocation, size );
				size  = Integer.parseInt( entry.getValue());
				literalTab.replace(entry.getKey(), rep );
				lastLocation = rep;
			}
	}
	public String CalculateHexa (String s1 , String s2)
	{
		int firstHexa = Integer.parseInt(s1,16);
		int secondHexa = Integer.parseInt(s2,16);
		int result = secondHexa -firstHexa;
		return  Integer.toHexString(result);
	}
	
	private void checkEnd(String line) {
		String opcode=null;
		if(!freeFormat) {
		line =completeSentence(line);
		opcode = line.substring(9,15);}
		else {
			String[] splited = line.trim().split("\\s+");
			opcode = splited[0];
		}
		if(opcode.trim().equals("end"))
			{
			  end=true;
			}
		else {
			outputPhase1.get(outputPhase1.size()-1).error = 13;
		}
	}


	public void Assemble(String s)
	{
		//check for start	
				s = completeSentence(s);
				op = null;
				if(freeFormat)
				op = freeFormatSplit(s);
				else {
					op = fixedFormatSplit(s);
				}
	}
	
	public Operation split(String s, String label,String opcode, String operand) {
		Errors error = new Errors();
		
		Operation op;
		int errorNum=0;
		
		int format = format(opcode.trim());
		
		if(freeFormat && opcode.charAt(0)=='+')
		{
			format=4;
			opcode=opcode.substring(1);
		}
		else if(s.charAt(8) == '+' ) {
			format=4;
		}
		int operandCount = operandCount(opcode.trim());
		errorNum = error.CheckErrors(s.charAt(8), opcode, label, operand, operandCount, symbTab, format,freeFormat);
		 
		//System.out.println("error num is" +errorNum);
		
		if(start == false && opcode.trim().equalsIgnoreCase("start"))
		{
			locationCounter = (operand.trim());
			start = true;
		}
		else if(locationCounter==null){
			locationCounter="0";
		}
		
		if(errorNum == 0 && !(label.trim().isEmpty()) )
			symbTab.put( label.trim(),(locationCounter) );
		
		if(operandCount == 1)
		{
			//System.out.println("hi"+locationCounter+"hi" );
			op= new Operation(opcode.trim(),label.trim(),format,operand.trim(),null,1, locationCounter.trim(), errorNum);
		}
		else {
			String operands[] = operand.split(",");
			op= new Operation(opcode.trim(),label.trim(),format,operands[0].trim(),operands[1].trim(),2, locationCounter, errorNum);
		}
		
		locationCounter = pcPlusFormat(locationCounter, format);
		int directiveLen = (int)directives(op);
		locationCounter = pcAndDirectives(directiveLen, locationCounter);
		literalCheck(operand.trim());
		outputPhase1.add(op);
        programLengthINT += format;
        programLengthINT += directiveLen;
		return op;
	}
	
	public String pcAndDirectives (int directive , String pc) {
		int location  = Integer.parseInt(pc, 16);
		String str  = Integer.toString(directive);
		int dir = Integer.parseUnsignedInt(str, 16);
		int res = location + dir;
 		return (Integer.toHexString(res));	
	}
	
 	public String pcPlusFormat(String locationCounter , int format) {
 		int pc  = Integer.parseInt(locationCounter, 16);
 		String str  = Integer.toString(format);
 		int formt = Integer.parseUnsignedInt(str, 16);
 		int res = pc + formt;
 		return (Integer.toHexString(res));

	}
	
	public double directives(Operation op) {
		
		ObjectGenerator obg = new ObjectGenerator(this);
		double length=0;
		if( op.opcode.trim().equalsIgnoreCase("byte") )
		{
			 String operand  = op.op1.trim();
			 length = operand.length() - 3;
			 if( operand.charAt(0) == 'x')
			 {
				 length = Math.ceil((double)length/2);
			 }
		} else if (op.opcode.trim().equalsIgnoreCase("word"))
		{
			for(char i: op.opcode.toCharArray() )
			{
				if(i == ',')
					length++;
			}
			length++;
			length = length*3;
		} else if( op.opcode.trim().equalsIgnoreCase("resb"))
		{
			length = Integer.parseInt(op.op1.trim() );
			
		} else if(op.opcode.trim().equalsIgnoreCase("resw"))
		{
			length = Integer.parseInt(op.op1.trim() )*3;
		} 
		else if( op.opcode.trim().equalsIgnoreCase("org"))
		{
			if(  obg.isNumeric(op.op1) ) 
				
				locationCounter = (Integer.toHexString(Integer.parseInt(op.op1))); //org takes decimal number then converts it to hexa
			
			else if ( expressionEvaluation(op.op1) != null)
			{
				String val = expressionEvaluation(op.op1);
				locationCounter = val;
			} else {
				locationCounter = symbTab.get(op.op1);
			}
			
			
		} else if( op.opcode.trim().equalsIgnoreCase("equ")) // *** a change is here ***
		{
			if(  obg.isNumeric(op.op1) ) 
				symbTab.replace(op.label, Integer.toHexString(Integer.parseInt(op.op1)));  //equ takes decimal and converts it to hexa
			else if( expressionEvaluation(op.op1) != null)
			{
				String val = expressionEvaluation(op.op1);
				this.symbTab.replace(op.label, val);
			}
			else
			{
				String val = symbTab.get(op.op1);
				this.symbTab.replace(op.label, val);
			}
			
				
		} else if( op.opcode.trim().equalsIgnoreCase("base") )
		{
			base = Integer.parseInt(op.op1.trim() );
			baseRelativeAddressing = true;
		} 
		else if (op.opcode.trim().equalsIgnoreCase("ltorg"))
		{
			ltorg = true;
			lcLtorg = locationCounter;
		}
		
		return length;		
	}
	
	
	public String expressionEvaluation( String str)
	{
		
		
		 if( str.contains("+"))  
		{
			 
			 String operands[] = str.split("\\+");
			 int len = operands.length;
			 int i=0;
			 int val = 0;
			 while( i< len)
			 {
				 val += Integer.parseUnsignedInt(evaluationHelper( operands[i]) , 16);
				 i++;
				 
			 }
	
	 	//	int val = Integer.parseUnsignedInt(s1, 16) +  Integer.parseUnsignedInt(s2, 16);

            String hexval = Integer.toHexString(val);
            return hexval;
			 
		    
		 
		}
		else if( str.contains("/") )
		{
			String operands[] = str.split("\\/");
			int len = operands.length;
			 int i=0;
			 int val = 0;
			 while( i< len)
			 {
				 val += Integer.parseUnsignedInt(evaluationHelper( operands[i]) , 16);
				 i++;
				 
			 }
	

	            String hexval = Integer.toHexString(val);
	            return hexval;

		}
		else if ( str.contains("*") )
		{
			String operands[] = str.split("\\*");
			int len = operands.length;
			 int i=0;
			 int val = 0;
			 while( i< len)
			 {
				 val += Integer.parseUnsignedInt(evaluationHelper( operands[i]) , 16);
				 i++;
				 
			 }
			
            String hexval = Integer.toHexString(val);
            return hexval;

		}
		else if ( str.contains("-") )
		{
			String operands[] = str.split("\\-");
			int len = operands.length;
			 int i=0;
			 int val = 0;
			 while( i< len)
			 {
				 val += Integer.parseUnsignedInt(evaluationHelper( operands[i]) , 16);
				 i++;
				 
			 }
			

            String hexval = Integer.toHexString(val);
            return hexval;

		}
		 
		 return null;
	}
	
	public String evaluationHelper( String s1)
	{
		
		ObjectGenerator obg = new ObjectGenerator(this);
		
		 String hexaValue1;
	     
		 if( obg.isNumeric(s1))
		 {
			hexaValue1 = Integer.toHexString(Integer.parseInt(s1));
		 } else
			 hexaValue1 = symbTab.get(s1);
	
		 return hexaValue1;	 
	}
	
			
	private int format(String opcode) {
		
		 if( loader.format2Instructions.contains(opcode))
			return 2;
		else if ( loader.directives.contains(opcode) )
			return 0;
		else return 3;
	}
	
	private int operandCount(String opcode)
	{
		if( loader.registerInstructions.contains(opcode) && !opcode.equalsIgnoreCase("tixr"))
			return 2;
		else return 1;
	}
	
	private String completeSentence(String s)
	{
		int length = s.length();
		while( length < 35)
		{
			s = s + " ";
			length++;
		}
		return s;
	}
	
	
	
	private void writeToListFile(SicAssembler assembler) 	throws IOException {
	    
	    BufferedWriter writer = new BufferedWriter(new FileWriter("listFile.txt"));
	    Errors error = new Errors();
	    
		 for(Operation o : assembler.outputPhase1 )
		    {
		    	writer.write(Integer.toHexString(Integer.parseInt(o.address,16)));
		    	
		    	if(o.label!=null)
		    		writer.write(" "+(o.label));
		    	else
		    		writer.write(" ");
		    	  
		    	 	writer.write(" "+(o.opcode));
		    
		    	if(o.op1!=null) 
		    	     writer.write(" "+(o.op1)+" ");
		    	else
		             writer.write(" ");
   	
		    	if(o.op2!=null)
		    		writer.write(" "+(o.op2));
		    	
		    	writer.write(System.getProperty( "line.separator" ));;
		    	
		    	if(o.error!=0) {
		    		errorExists=true;	    		
		    		writer.write(error.printError(o.error));
		    		writer.write(System.getProperty( "line.separator" ));
		    	}
		    }
		 
		 writer.write(System.getProperty( "line.separator" ));
		 
		writer.write("Symbol Table"+String.format("%n"));
		 for( Map.Entry<String, String> entry : assembler.symbTab.entrySet() )
		 {
			writer.write(entry.getKey() +" " + entry.getValue()+ String.format("%n"));
			
		 }
		 
		 
		 
		 
		 writer.write("Literal Table"+String.format("%n"));
		 for( Map.Entry<String, String> entry : assembler.literalTab.entrySet() )
		 {
			 int len = entry.getKey().length();
			
			writer.write(entry.getKey().substring(3 , len-1) +" " + entry.getValue()+ String.format("%n"));
			
		 }
	    	    
	    writer.close();
	}
	
	void writeToConsole(SicAssembler assembler) {
		
		Errors error = new Errors();
		 for(Operation o : assembler.outputPhase1 )
		    {
		    	System.out.print(Integer.toHexString(Integer.parseInt(o.address,16)) +" "+ o.label +" "
		    			+ o.opcode + " "+ o.op1 );
		    	if( o.op2 != null)
		    		System.out.print(" " + o.op2);
		    	System.out.println("");
		    	if( o.error != 0)
		    	{   
		    		String theError =error.printError(o.error);
		    		System.out.println(theError);
		    	}
		    	
		    }
		 System.out.println("Symbol Table");
		 for( Map.Entry<String, String> entry : assembler.symbTab.entrySet() )
		 {
			 System.out.println(entry.getKey() +" " + entry.getValue());
		 }
		 System.out.println(" ");
		 System.out.println("Literal Table");
		 for( Map.Entry<String, String> entry : assembler.literalTab.entrySet() )
		 {
			 int len = entry.getKey().length();
			 System.out.println(entry.getKey().substring(3, len-1) +" " + entry.getValue());
			 
		 }
	}
	
	public static void main(String[] args) throws IOException 
	{
		SicAssembler assembler = new SicAssembler();
		ObjectGenerator generator = new ObjectGenerator(assembler);
		assembler.read();
		assembler.writeToConsole(assembler);
		assembler.writeToListFile(assembler);
		if(!assembler.errorExists) {
		generator.GenerateObjectFile();
		generator.writeToObjectFile();}
	}

}
