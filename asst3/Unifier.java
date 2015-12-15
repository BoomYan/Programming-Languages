package academicTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import test.Test;

/**
 * Each <tt>Type</tt> has 2 static methods: 
 * <p> The <tt>isMe</tt> method is to identify 
 * which <tt>Type</tt> the input String should be, 
 * and check if this string is valid.
 * <p>The <tt>construct</tt> method is to construct a specific
 * <tt>Type</tt> from the input String.
 */
abstract class Type {
	
	private static final Map<String, Type> MAP = new HashMap<>();

	public static Type construct(String str) {
		if (MAP.containsKey(str)) {
			return MAP.get(str);
		}
		Type result = null;
		if (VarType.isMe(str)) {
			result = VarType.construct(str);
		}
		else if (PrimType.isMe(str)) {
			result = PrimType.construct(str);
		}
		else if (FuncType.isMe(str)) {
			result = FuncType.construct(str);
		}
		else if (ListType.isMe(str)) {
			result = ListType.construct(str);
		}
		if (result == null) {
			throw new IllegalArgumentException("ERR");
		}
		MAP.put(str, result);
		return result;
	}
	//used to parse string
	public static boolean isMe(String str) {
		if (str.length() ==  0) {
			return false;
		}
		return VarType.isMe(str) 
				|| FuncType.isMe(str)
				|| PrimType.isMe(str)
				|| ListType.isMe(str);
	}
}

class VarType extends Type {
	
	public static boolean isMe(String str) {
		if (str == null || str.length() < 2 || str.charAt(0) != '`') {
			return false;
		}
		return str.matches("`[a-zA-Z][a-zA-Z0-9]*");
	}

	public static VarType construct(String str) {
		return new VarType(str);
	}

	public final String name;
	
	VarType(String str) {
		this.name = str.substring(1);
	}

}

abstract class FinalType extends Type {
	public static boolean isMe(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		return PrimType.isMe(str)
				|| FuncType.isMe(str)
				|| ListType.isMe(str);
	}
}

//funcType and listType don't have a name;
class FuncType extends FinalType {
	private static char LEFT_BRACE = '(';
	private static char RIGHT_BRACE = ')';
	private static char COMMA = ',';
	private static String ARROW = "->";
	
	public static boolean isMe(String str) {
		try {
			FuncType.construct(str);
		} catch (RuntimeException re) {
			return false;
		}
		return true;
	}
	
	public static FuncType construct(String str) {
		FuncType result = new FuncType();
		if (str.charAt(0) != LEFT_BRACE) {
			throw new IllegalArgumentException();
		}
		int level = 0;
		int i = 0;
		for (; i < str.length(); i++) {
			if (str.charAt(i) == LEFT_BRACE) {
				level++;
			}
			if (str.charAt(i) == RIGHT_BRACE) {
				level--;
			}
			if (level == 0) {
				result.parTypes = constructArgList(str.substring(0, i + 1));
				break;
			}
		}
		if (!str.substring(i + 1, i + 3).equals(ARROW)) {
			throw new IllegalArgumentException();
		}
		i = i + 3;
		result.returnType = Type.construct(str.substring(i));
		return result;
	}
	
	//the Argument list has '('')'
	private static List<Type> constructArgList(String str) {
		List<Type> result = new ArrayList<>();
		if (str.charAt(0) != LEFT_BRACE || str.charAt(str.length() - 1) != RIGHT_BRACE) {
			throw new IllegalArgumentException();
		}
		if (str.length() == 2) {
			return result;
		}
		int level = 0;
		int i = 0;
		int argStartIndex = 1;
		for (; i < str.length(); i++) {
			if (str.charAt(i) == LEFT_BRACE) {
				level++;
			}
			if (str.charAt(i) == RIGHT_BRACE) {
				level--;
			}
			if (level == 1 && str.charAt(i) == COMMA) {
				//found an argument
				result.add(Type.construct(str.substring(argStartIndex, i)));
				argStartIndex = i + 1;
			}
			if (level == 0 && i == str.length() - 1) {
				result.add(Type.construct(str.substring(argStartIndex, i)));
			}
		}
		if (level != 0) {
			throw new IllegalArgumentException();
		}
		return result;
	}
	public List<Type> parTypes = new ArrayList<>();
	public Type returnType = null;
}

class PrimType extends FinalType {
	
	static final List<PrimType> TYPES = new ArrayList<>();
	static {
		TYPES.add(new PrimType("int"));
		TYPES.add(new PrimType("real"));
		TYPES.add(new PrimType("str"));
	}
	private static final List<String> TYPENAMES = new ArrayList<>();
	static {
		TYPENAMES.add(new String("int"));
		TYPENAMES.add(new String("real"));
		TYPENAMES.add(new String("str"));
	}
	public static boolean isMe(String str) {
		return TYPENAMES.contains(str);
	}

	public static PrimType construct(String str) {
		return TYPES.get(TYPENAMES.indexOf(str));
	}
	public final String name;
	PrimType (String str) {
		this.name = str;
	}

}

class ListType extends FinalType {

	public static boolean isMe(String str) {
		if (str == null || str.length() < 4) {
			return false;
		}
		if (str.charAt(0) != '[') {
			return false;
		}
		if (!Type.isMe(str.substring(1, str.length() - 1))) {
			return false;
		}
		if (str.charAt(str.length() - 1) != ']') {
			return false;
		}
		return true;
	}

	public static ListType construct(String str) {
		ListType result = new ListType();
		result.parType = Type.construct(str.substring(1, str.length() - 1));
		return result;
	}
	
	Type parType = null;
}

public class Unifier {
	
	public static String eliminateSpaces(String str) {
		//check invalid space
		if (str.matches(".*[`a-zA-Z0-9]+\\s+[a-zA-Z0-9]+.*")) {
			throw new IllegalArgumentException("ERR");
		}
		return str.replaceAll("\\s+","");
	}
	
	public static void main(String[] args) {
		Unifier u = new Unifier();
		try(@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			boolean continueReading = true; 
			while (continueReading) {
				try {
					Type type1 = null, type2 =null;
					String input = reader.readLine();
					input = eliminateSpaces(input);
			        if (input.equals("QUIT")) {
			        	return;
			        }
			        String[] types = input.split("&");
			        if (types.length != 2) {
			        	throw new IllegalArgumentException("ERR");
			        }
			        type1 = Type.construct(types[0]);
			        type2 = Type.construct(types[1]);
			        u.unify(type1, type2);
					Test.print(u.getType(type1));
				} catch (IllegalArgumentException iae) {
					Test.print(iae.getMessage());
					iae.printStackTrace();
					return;
				} catch (RuntimeException re) {
					Test.print("SHIT");
					re.printStackTrace();
					return;
				}
			}
		} catch (IOException ioe) {
			Test.print("ERR");
			ioe.printStackTrace();
		} 
	}

	//store binding relation
	//each type either bind to an equal set of types
	//or bind to a FinalType
	private final Map<VarType, Set<VarType>> setMap;
	private final Map<VarType, FinalType> finalMap;
	
	public Unifier() {
		setMap = new HashMap<>();
		finalMap = new HashMap<>();
	}

	public void unify (final Type type1, final Type type2) {
		if (type1 == null || type2 == null) {
			throw new IllegalArgumentException();
		}
		//will return null if inappropriate
		Set<VarType> set1 = constructEqualSet(type1);
		Set<VarType> set2 = constructEqualSet(type2);
		if (set1 != null && set2 != null) {
			if (set1 != set2) {
				bind(set1, set2);
			}
			return;
		}
		if (set1 != null && (FinalType.isMe(getType(type2)))) {
			bind(set1, (FinalType)Type.construct(getType(type2)));
			return;
		}
		if (set2 != null && (FinalType.isMe(getType(type1)))) {
			bind(set2, (FinalType)Type.construct(getType(type1)));
			return;
		}
		
		Type realType1 = getFinalType(type1);
		Type realType2 = getFinalType(type2);

		if (realType1 instanceof ListType && realType2 instanceof ListType) {
			ListType listType1 = (ListType)realType1;
			ListType listType2 = (ListType)realType2;
			unify(listType1.parType, listType2.parType);
			return;
		}
		if (realType1 instanceof FuncType && realType2 instanceof FuncType) {
			FuncType funcType1 = (FuncType)realType1;
			FuncType funcType2 = (FuncType)realType2;
			if (funcType1.parTypes.size() != funcType2.parTypes.size()) {
				throw new IllegalArgumentException("Bottom");
			}
			for (int i = 0; i < funcType1.parTypes.size(); i++) {
				unify(funcType1.parTypes.get(i), funcType2.parTypes.get(i));
			}
			unify(funcType1.returnType, funcType2.returnType);
			return;
		}
		if (equals(type1, type2)) {
			return;
		}
		throw new IllegalArgumentException("Bottom");
	}
	
	//check if unbound type is actually bound
	private Type getFinalType(Type type) {
		if (finalMap.containsKey(type)) {
			return finalMap.get(type);
		}
		return type;
	}
	
	private void bind (Set<VarType> set1, Set<VarType> set2) {
		set1.addAll(set2);
		//for c++ can change &set2 to &set1????
		for (VarType type : set2) {
			setMap.put(type, set1);
		}
	}
	
	private void bind (Set<VarType> set1, FinalType finalType) {
		//bind types in set1 to primType
		for (VarType type : set1) {
			if (finalMap.containsKey(type) && !equals(finalMap.get(type), finalType)) {
				throw new IllegalArgumentException("BOTTOM");
			}
			//update primMap
			finalMap.put(type, finalType);
			//update setMap
			setMap.remove(type);
		}
	}
	
	//actually point to the same Type
	private boolean equals(Type type1, Type type2) {
		return getType(type1).equals(getType(type2));
	}
	
	private Set<VarType> constructEqualSet(Type type) {
		if (!(type instanceof VarType) || setMap.containsKey(type) || finalMap.containsKey(type)) {
			return null;
		}
		Set<VarType> set = new TreeSet<VarType>(new Comparator<VarType>(){
			@Override
			public int compare(VarType o1, VarType o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		set.add((VarType)type);
		setMap.put((VarType)type, set);
		return set;
	}
	
	//checked for circle
	Set<Type> gettedTypes = new HashSet<Type>();
	
	public String getType(Type type) {
		if (gettedTypes.contains(type)) {
			throw new IllegalArgumentException("BOTTOM");
		}
		gettedTypes.add(type);
		String result = getTypeHelper(type);
		gettedTypes.remove(type);
		return result;
	}
	
	//for output, convert a type to output String
	public String getTypeHelper (Type type) {
		if (type instanceof VarType) {
			return getVarType((VarType)type);
		}
		if (type instanceof FuncType) {
			return getFuncType((FuncType)type);
		}
		if (type instanceof PrimType) {
			return getPrimType((PrimType)type);
		}
		if (type instanceof ListType) {
			return getListType((ListType)type);
		}
		throw new IllegalArgumentException("BOTTOM");
	}
	
	public String getVarType (VarType type) {
		if (finalMap.containsKey(type)) {
			return getType(finalMap.get(type));
		}
		if (setMap.containsKey(type)) {
			return String.format("`%s", setMap.get(type).iterator().next().name);
		}
		return String.format("`%s", type.name);
	}
	
	public String getPrimType (PrimType type) {
		return type.name;
	}
	
	public String getListType (ListType type) {
		return String.format("[%s]", getType(type.parType));
	}
	
	public String getFuncType (FuncType funcType) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		if (funcType.parTypes.size() != 0) {
			for (Type type : funcType.parTypes) {
				sb.append(getType(type));
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")");
		sb.append("->");
		sb.append(getType(funcType.returnType));
		return sb.toString();
	}
	
}
