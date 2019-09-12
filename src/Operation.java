public class Operation{
	
	String opcode;
	String label;
	int format;
	String op1;
	String op2;
	int    numberOfOperands;
	String   address;
	int error=0;
	
	
	Operation(String opcode,String label,int format,String op1,String op2, int operandCount, String locationCounter, int error)
		{
			this.opcode=opcode;
			this.label=label;
			this.format=format;
			this.op1=op1;
			this.op2=op2;
			this.numberOfOperands = operandCount;
			this.address = locationCounter;
			this.error = error;
		}
	}