import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Load {
	
	Map<String,String> instructions = new HashMap<String, String>();
	Map<String,String> registers = new HashMap<String,String>();

	Set<String> directives = new HashSet<String>(); 
	Set<String> format2Instructions = new HashSet<String>();
	Set<String> registerInstructions = new HashSet<String>();
	
	Load()
	{
		instructions.put("rmo", "10101100");
		instructions.put("lda", "00000000");
		instructions.put("lds", "01101100");
		instructions.put("ldt", "01110100");
		instructions.put("ldx", "00000100");
		instructions.put("ldb", "01101000");
		instructions.put("sta", "00001100");
		instructions.put("sts", "01111100");
		instructions.put("stt", "10000100");
		instructions.put("stx", "00010000");
		instructions.put("stb", "01111000");
		instructions.put("mul", "0");
		instructions.put("ldch", "01010000");
		instructions.put("stch", "01010100");
		instructions.put("add", "00011000");
		instructions.put("sub", "00011100");
		instructions.put("addr", "10010000");
		instructions.put("subr", "10010100");
		instructions.put("comp", "00101000");
		instructions.put("compr", "10100000");
		instructions.put("j",     "00111100");
		instructions.put("jeq", "00110000");
		instructions.put("jlt", "00111000");
		instructions.put("jgt", "00110100");
		instructions.put("tix", "00101100");
		instructions.put("tixr", "10111000");
	
		format2Instructions.add("rmo");
		format2Instructions.add("subr");
		format2Instructions.add("compr");
		format2Instructions.add("tixr");
		format2Instructions.add("addr");
		
		directives.add("start");
		directives.add("end");
		directives.add("byte");
		directives.add("word");
		directives.add("resw");
		directives.add("resb");
		directives.add("equ");
		directives.add("org");
		directives.add("base");
		directives.add("ltorg");
		
		registerInstructions.add("rmo");
		registerInstructions.add("addr");
		registerInstructions.add("subr");
		registerInstructions.add("compr");
		registerInstructions.add("tixr");
		
		registers.put("a","0");
		registers.put("x","1");
		registers.put("b","3");
		registers.put("s","4");
		registers.put("t","5");
		registers.put("f","6");
	

		
	}
	public Set<String> getRegisterInstructions() {
		return registerInstructions;
	}


	

	public Map<String, String> getRegisters() {
		return registers;
	}
	public void setRegisters(Map<String, String> registers) {
		this.registers = registers;
	}
	public Set<String> getDirectives() {
		return directives;
	}

	public Map<String, String> getInstructions() {
		return instructions;
	}

	public Set<String> getFormat2Instructions() {
		return format2Instructions;
	}

	//implement function to load all directives in Map
	//implement fn to load all instructions in map
}
