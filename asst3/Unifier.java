package academicTest;

import java.util.*;

import test.Test;

abstract class Type {
	
	private static final Map<String, Type> MAP = new HashMap<>();

	
	public static Type construct(String str) {
		//TODO: delete all possible spaces in str except those illegal;
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
		for (int i = 1; i < str.length(); i++) {
			if (!Character.isLetter(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static VarType construct(String str) {
		return new VarType(str);
	}
	
}

class FinalType extends Type {
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
	List<Type> parTypes = new ArrayList<>();
	Type returnType = null;
	public FuncType() {
	}

	public static boolean isMe(String str) {
		if (str == null || str.length() < 5) {
			return false;
		}
		int j = 0;
		while (j < str.length() && str.charAt(j) != ')') {
			j++;
		}
		if (j == str.length()) {
			return false;
		}
		if (!isArgList(str.substring(0, j + 1))) {
			Test.print("not argslist");
			return false;
		}
		if (str.length() <= j + 3) {
			return false;
		}
		if (!str.substring(j + 1, j + 3).equals("->")) {
			return false;
		}
		j = j + 3;
		if (!Type.isMe(str.substring(j))) {
			Test.print("return type");
			return false;
		}
		return true;
	}
	
	private static boolean isArgList(String str) {
		if (str == null || str.length() < 2) {
			return false;
		}
		if (str.charAt(0) != '(') {
			return false;
		}
		if (str.length() == 2 && str.charAt(1) == ')') {
			return true;
		}
		String[] args = str.substring(1,str.length() - 1).split(",");
		String str2 = new String();
		for (String arg : args) {
			str2 += arg;
			if (!Type.isMe(arg)) {
				return false;
			}
		}
		if (str.length() != (str2.length() + 2 + args.length - 1)) {
			Test.print("lengthhhhhhh");
			return false;
		}
		if (str.charAt(str.length() - 1) != ')') {
			return false;
		}
		return true;
	}

	public static FuncType construct(String str) {
		FuncType result = new FuncType();
		int j = str.indexOf(")");
		String[] args = str.substring(1, j).split(",");
		for (String arg : args) {
			result.parTypes.add(Type.construct(arg));
		}
		j = j + 3;
		result.returnType = Type.construct(str.substring(j));
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
	//or bind to a primType
	private final Map<Type, Set<Type>> setMap;
	private final Map<Type, PrimType> primMap;
	
	public Unifier() {
		setMap = new HashMap<>();
		primMap = new HashMap<>();
		for (PrimType p : PrimType.TYPES) {
			primMap.put(p, p);
		}
	}

	public void unify (final Type type1, final Type type2) {
		if (type1 == null || type2 == null) {
			throw new IllegalArgumentException();
		}
		//will return if inappropriate
		constructSet(type1);
		constructSet(type2);
		Set<Type> set1 = setMap.get(type1);
		Set<Type> set2 = setMap.get(type2);
		if (set1 != null && set2 != null) {
			if (set1 != set2) {
				bind(set1, set2);
			}
			return;
		}
		if (set1 != null && (PrimType.isMe(getType(type2)))) {
			bind(set1, PrimType.construct(getType(type2)));
			return;
		}
		if (set2 != null && (PrimType.isMe(getType(type1)))) {
			bind(set2, PrimType.construct(getType(type1)));
			return;
		}
		if (type1 instanceof ListType && type2 instanceof ListType) {
			ListType listType1 = (ListType) type1;
			ListType listType2 = (ListType) type2;
			unify(listType1.parType, listType2.parType);
			return;
		}
		if (type1 instanceof FuncType && type2 instanceof FuncType) {
			FuncType funcType1 = (FuncType) type1;
			FuncType funcType2 = (FuncType) type2;
			if (funcType1.parTypes.size() != funcType2.parTypes.size()) {
				throw new IllegalArgumentException("Bottom");
			}
			for (int i = 0; i < funcType1.parTypes.size(); i++) {
				unify(funcType1.parTypes.get(i), funcType2.parTypes.get(i));
			}
			unify(funcType1.returnType, funcType2.returnType);
			return;
		}
		//type1 and type2 point to same PrimType
		if ((PrimType.isMe(getType(type2))) && primMap.get(type1) == PrimType.construct(getType(type2))) {
			return;
		}
		if ((PrimType.isMe(getType(type1))) && primMap.get(type2) == PrimType.construct(getType(type1))) {
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
	
	private void bind (Set<Type> set1, PrimType primType) {
		//bind types in set1 to primType
		for (Type type : set1) {
			if (primMap.containsKey(type) && !primMap.get(type).name.equals(primType.name)) {
				throw new IllegalArgumentException("BOTTOM");
			}
			//update primMap
			primMap.put(type, primType);
			//update setMap
			setMap.remove(type);
		}
	}
	
	private void constructSet (Type type) {
		if (!(type instanceof VarType) || setMap.containsKey(type) || primMap.containsKey(type)) {
			return;
		}
		Set<Type> set = new TreeSet<Type>(new Comparator<Type>(){
			@Override
			public int compare(Type o1, Type o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		set.add(type);
		setMap.put(type,  set);
	}
	
	public String getType (Type type) {
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
		if (primMap.containsKey(type)) {
			return primMap.get(type).name;
		}
		if (setMap.containsKey(type)) {
			return "`" + setMap.get(type).iterator().next().name;
		}
		throw new IllegalArgumentException("BOTTOM");
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
	
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Unifier u = new Unifier();
		while (true) {
			try {
				Type type1 = null, type2 =null;
				Scanner s = new Scanner(System.in);
				String input = s.nextLine();
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
			}
			catch (IllegalArgumentException iae) {
				Test.print(iae.getMessage());
				iae.printStackTrace();
				return;
			}
			catch (RuntimeException re) {
				Test.print("BOTTOM");
				re.printStackTrace();
				return;
			}
		}

		
	}

}
