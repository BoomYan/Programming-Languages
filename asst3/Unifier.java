package academicTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import test.Test;

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
	
	public String name;
	//used to parse string
	//if isMe not isMe for all, throw new Exception("ERR")
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

	VarType(String str) {
		this.name = str.substring(1);
	}

	public static boolean isMe(String str) {
		if (str == null || str.length() < 2 || str.charAt(0) != '`') {
			return false;
		}
		return str.matches("`[a-zA-Z]+");
	}

	public static VarType construct(String str) {
		return new VarType(str);
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
	
	List<Type> parTypes = new ArrayList<>();
	Type returnType = null;
	
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
				//found a argument
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
	PrimType (String str) {
		this.name = str;
	}
	public static boolean isMe(String str) {
		return TYPENAMES.contains(str);
	}

	public static PrimType construct(String str) {
		return TYPES.get(TYPENAMES.indexOf(str));
	}
	
}

class ListType extends FinalType {
	Type parType = null;

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
}

public class Unifier {
	
	//store binding relation
	//each type either bind to an equal set of types
	//or bind to a FinalType
	private final Map<Type, Set<Type>> setMap;
	private final Map<Type, FinalType> finalMap;
	
	public Unifier() {
		setMap = new HashMap<>();
		finalMap = new HashMap<>();
		for (PrimType p : PrimType.TYPES) {
			finalMap.put(p, p);
		}
	}

	public void unify (final Type type1, final Type type2) {
		if (type1 == null || type2 == null) {
			throw new IllegalArgumentException();
		}
		//will return if inappropriate
		constructEqualSet(type1);
		constructEqualSet(type2);
		Set<Type> set1 = setMap.get(type1);
		Set<Type> set2 = setMap.get(type2);
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

		if (ListType.isMe(getType(type1)) && ListType.isMe(getType(type2))) {
			ListType listType1 = (ListType)Type.construct(getType(type1));
			ListType listType2 = (ListType)Type.construct(getType(type2));
			unify(listType1.parType, listType2.parType);
			return;
		}
		if (FuncType.isMe(getType(type1)) && FuncType.isMe(getType(type2))) {
			FuncType funcType1 = (FuncType)Type.construct(getType(type1));
			FuncType funcType2 = (FuncType)Type.construct(getType(type2));
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
	
	private void bind (Set<Type> set1, Set<Type> set2) {
		set1.addAll(set2);
		//for c++ can change &set2 to &set1????
		for (Type type : setMap.keySet()) {
			if (setMap.get(type) == set2) {
				setMap.put(type, set1);
			}
		}
	}
	
	private void bind (Set<Type> set1, FinalType finalType) {
		//bind types in set1 to primType
		for (Type type : set1) {
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
	
	private boolean constructEqualSet(Type type) {
		if (!(type instanceof VarType) || setMap.containsKey(type) || finalMap.containsKey(type)) {
			return false;
		}
		Set<Type> set = new TreeSet<Type>(new Comparator<Type>(){
			@Override
			public int compare(Type o1, Type o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		set.add(type);
		setMap.put(type, set);
		return true;
	}
	
	//checked for circle
	Set<Type> gettedTypes = new HashSet<Type>();
	
	public String getType(Type type) {
		if (gettedTypes.contains(type)) {
			throw new IllegalArgumentException("BOTTOM");
		}
		else {
			gettedTypes.add(type);
		}
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
			if (finalMap.get(type) instanceof PrimType) {
				return finalMap.get(type).name;
			}
			else {
				return getType(finalMap.get(type));
			}
		}
		if (setMap.containsKey(type)) {
			return "`" + setMap.get(type).iterator().next().name;
		}
		return "`" + type.name;
	}
	
	public String getPrimType (PrimType type) {
		return type.name;
	}
	
	public String getListType (ListType type) {
		return "[" + getType(type.parType) + "]";
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
	
	public static String eliminateSpace(String str) {
		//check invalid space
		if (str.matches(".*[`_a-zA-Z]+\\s+[a-zA-Z]+.*")) {
			throw new IllegalArgumentException("SHIT");
		}
		return str.replaceAll("\\s+","");
	}
	
	public static void main(String[] args) {
		Unifier u = new Unifier();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				try {
					Type type1 = null, type2 =null;
					String input = reader.readLine();
					if (input.length() == 0) {
						continue;
					}
			        if (input.equals("QUIT")) {
			        	return;
			        }
			        String[] types = input.split("&");
			        if (types.length != 2) {
			        	throw new IllegalArgumentException("ERR");
			        }
			        String arg1 = eliminateSpace(types[0]);
			        String arg2 = eliminateSpace(types[1]);
			        type1 = Type.construct(arg1);
			        type2 = Type.construct(arg2);
			        u.unify(type1, type2);
					Test.print(u.getType(type1));
				} catch (IllegalArgumentException iae) {
					Test.print(iae.getMessage());
					iae.printStackTrace();
					return;
				} catch (RuntimeException re) {
					Test.print("BOTTOM");
					re.printStackTrace();
					return;
				}
			}
		} catch (IOException ioe) {
			Test.print("ERR");
			ioe.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					Test.print("ERR");
					ioe.printStackTrace();
				}
			}
		}
	}

}
