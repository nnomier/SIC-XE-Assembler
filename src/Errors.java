import java.util.Map;

public  class Errors {
	
	Load loader = new Load();
	
	 public int CheckErrors (char prefix,String op, String label, String operand, int operandCount, Map<String,String> symbTab, int format, boolean freeFormat) {
		 
		if(Misplaced(label))
			return 1;
				
		if(Misplaced(op))
			return 2;
		
		if(Misplaced(operand))
			return 3;
		
		if(duplicate(label, symbTab))
			return 4;
		
		if(illegalLabel(op, label))
			return 5;
		
		if(!Prefix(prefix,label,format,freeFormat))
			return 7;
		
		if(unrecognizedOp(op))
			return 8;
		
		if(!Hexa(op,operand))
			return 10;
		
		if(!ValidFormat4(op, format))
			return 11;
		
		if(!ValidRegisters(operand, op))
			return 12;		
		return 0;
	}
	

	public boolean Misplaced(String s)
	{
		if(s.charAt(0)==' ' && !s.trim().isEmpty())
			return true;
		return false;
	}
	
	public boolean duplicate(String lbl,Map<String,String> symbTab)
	{
		if(symbTab.containsKey(lbl.trim()))
			return true;
		return false;	
	}
	
	public boolean illegalLabel(String opcode,String label)
	{  
		String[] noLabel= {"org","end","base","nobase","ltorg"};
		for(int i=0;i<noLabel.length;i++)
		{
			if(opcode.trim().equals(noLabel[i]) && !label.trim().isEmpty())
			{ 
				return true;
			}
		}
		
		return false;
	}
	
	public boolean unrecognizedOp (String op)
	{
		if(loader.getInstructions().containsKey(op.trim()) || loader.getDirectives().contains(op.trim()))
			return false;
	
		return true;
	}
	
	 public boolean Hexa(String op, String operand) {
		if(!op.trim().equals("byte") && !op.trim().equals("word") )
			return true;
		String operandT = operand.trim();
		//X'AB CD' needs to be checked
		if(operandT.charAt(0)=='x')
		{	 
			String hex = operandT.substring(2,operandT.length()-1);
		
			if(!hex.matches("[0-9a-f]+"))
			{   
				return false;
			}
		}
		return true;
	}
	
	public boolean ValidFormat4 (String op , int format)
	{
		if(format!=4)
			return true;
		
		Load loader = new Load(); 
		if(loader.getDirectives().contains(op.trim()) || loader.getFormat2Instructions().contains(op.trim()))
		    	  return false;	  
		return true;
	}
	

	public boolean ValidRegisters(String operand,String op)
	{
		if(!(loader.getRegisterInstructions().contains(op.trim())))
		{ 
			return true;
		}
		if(op.trim().equals("tixr") && !(loader.getRegisters().containsKey(operand.trim()))) 
			return false;
			
		if(op.trim().equals("tixr") && operand.trim().contains(","))
			return false;
			// Check if 2 operands available for 2 registers operations
		 if(!op.trim().equals("tixr"))
		 {	String operands[] = operand.trim().split(",");
		if((!loader.getRegisters().containsKey(operands[0])) || (!loader.getRegisters().containsKey(operands[1])) )
			return false;
		 }
		return true;
	}
	
	public boolean Prefix (char prefix,String lbl,int format,boolean freeFormat)
	{
		if(prefix!=' ' && format!=4 && !freeFormat)
			return false;
		return true;
	}
	
	
String  printError(int error)
	
	{	if( error == 1)
			return "misplaced  label"; 
		else if(error == 2 )
			return ("missing or misplaced operation mnemonic");
		else if( error == 3)
			return ("missing or misplaced operand field");
		else if (error == 4)
			return ("duplicate label definition");
		else if (error == 5)
			return ("this statement can’t have a label");
		else if(error == 6)
			return ("this statement can’t have an operand");
		else if(error == 7)
			return ("wrong g operation prefix");
		else if ( error == 8)
			return ("unrecognized operation code");
		else if(error == 9)
			return ("undefined symbol in operand");
		else if(error == 10)
			return ("not a hexadecimal string");
		else if(error == 11)
			return ("can’t be format 4 instruction");
		else if(error == 12)
			return ("illegal address for a register ");
		else 
			return ("missing END statement ");
		
		
	}
}
