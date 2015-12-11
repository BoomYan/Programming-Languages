#include <iostream>
#include <stdlib.h>
#include <sstream>
#include <string>
#include <map>
#include <set>
#include <vector>
#include <regex>
#include <unordered_set>

using namespace std;

class Type
{
private:
	//hold the value of Type actually
	static map<string, Type*> parsedTypeMap;

public:
	static Type* construct(string str);
	virtual void f() {}
	string name;
	static bool isMe(string str);
};

class VarType : public Type {
public:
	static bool isMe(string str);
	static VarType* construct(string str);
	string name;
	VarType(string str);
};

class FinalType : public Type {
public:
	static bool isMe(string str);
	virtual void f() {}
};

class FuncType : public FinalType {
private:
	const static char LEFT_BRACE;
	const static char RIGHT_BRACE;
	const static char COMMA;
	const static string ARROW;
	static vector<Type*> constructArgList(string str);
public:
	static bool isMe(string str);
	static FuncType* construct(string str);
	vector<Type*> parTypes;
	Type* returnType;
};

class ListType : public FinalType {
public:
	static bool isMe(string str);
	static ListType* construct(string str);
	Type* parType;
};

class PrimType : public FinalType {
public:
	static bool isMe(string str);
	static PrimType* construct(string str);
	string name;
	PrimType(string str);
};

class Unifier {
private:
	map<VarType*, set<VarType*>*> setMap;
	map<VarType*, FinalType*> finalMap;
	struct classcomp {
  		bool operator() (const VarType& lhs, const VarType& rhs) const
  		{return lhs.name.compare(rhs.name);}
	};
	set<VarType, classcomp> gettedTypes;
	void bind(set<VarType*>* set1, set<VarType*>* set2);
	void bind(set<VarType*>* set1, FinalType* finalType);
	bool equals(Type* type1, Type* type2);
	void constructEqualSet(Type* type);
	string getTypeHelper(Type* type);
	string getVarType (VarType* type);
	string getPrimType (PrimType* type);
	string getListType (ListType* type);
	string getFuncType (FuncType* type);
public:
	void unify(Type* type1, Type* type2);
	string getType(Type* type);
};

map<string, Type*> Type::parsedTypeMap;

bool Type::isMe(string str) {
	if (str.length() == 0) {
		return false;
	}
	return VarType::isMe(str) 
			|| FuncType::isMe(str)
			|| PrimType::isMe(str)
			|| ListType::isMe(str);	
}

Type* Type::construct(string str) {


	if (parsedTypeMap.find(str) != parsedTypeMap.end()) {
		return parsedTypeMap[str];
	}
	Type* result = NULL;
	if (VarType::isMe(str)) {
		result = VarType::construct(str);
	}
	else if (PrimType::isMe(str)) {
		result = PrimType::construct(str);
	}
	else if (FuncType::isMe(str)) {
		result = FuncType::construct(str);
	}
	else if (ListType::isMe(str)) {
		result = ListType::construct(str);
	}
	if (result == NULL) {
		throw "ERR";
	}
	parsedTypeMap[str] = result;
	return result;
}

bool VarType::isMe(string str) {
	if (str.length() < 2 || str[0] != '`') {
		return false;
	}
	regex r("`[a-zA-Z][a-zA-Z0-9]*");
	return regex_match(str, r);	
}
VarType* VarType::construct(string str) {
	// cout<<"varType construct+" + str<<endl;
	return new VarType(str);
}
VarType::VarType(string str) {
	this->name = str.substr(1, str.length() - 1);
}

bool FinalType::isMe(string str) {
	return PrimType::isMe(str)
		|| FuncType::isMe(str)
		|| ListType::isMe(str);
}

const char FuncType::LEFT_BRACE = '(';
const char FuncType::RIGHT_BRACE = ')';
const char FuncType::COMMA = ',';
const string FuncType::ARROW = "->";

bool FuncType::isMe(string str) {
	//TODO
	try {
		construct(str);
	}
	catch(const std::runtime_error& re)
	{
	    return false;
	}
	catch(const std::exception& ex)
	{
		return false;
	}
	catch(const char * e) {
		return false;
	}
	return true;	
}
FuncType* FuncType::construct(string str) {
	FuncType* result = new FuncType();
	// cout<<"funcType construct start "<<str<<endl;
	if (str[0] != LEFT_BRACE) {
		throw "err";
	}
	int level = 0;
	int i = 0;
	for (; i < str.length(); i++) {
		// cout<<"funcType  "<<i<<endl;
		if (str[i] == LEFT_BRACE) {
			level++;
		}
		if (str[i] == RIGHT_BRACE) {
			level--;
		}
		if (level == 0) {
			result->parTypes = constructArgList(str.substr(0, i + 1));
			break;
		}
	}
	if (!(str.substr(i + 1, 2) == ARROW)) {
		throw "err";
	}
	i = i + 3;
	// cout<<"funcType returnType started  "<<str.substr(i, str.length() - i)<<endl;
	result->returnType = Type::construct(str.substr(i, str.length() - i));
	// cout<<"funcType returnType ended"<<endl;
	return result;
}

vector<Type*> FuncType::constructArgList(string str) {
	//TODO
	vector<Type*> result;
	if (str[0] != LEFT_BRACE || str[str.length() - 1] != RIGHT_BRACE) {
		throw "err";
	}
	if (str.length() == 2) {
		return result;
	}
	int level = 0;
	int i = 0;
	int argStartIndex = 1;
	for (; i < str.length(); i++) {
		// cout<<"arglist  "<<i<<endl;
		if (str[i] == LEFT_BRACE) {
			level++;
		}
		if (str[i] == RIGHT_BRACE) {
			level--;
		}
		if (level == 1 && str[i] == COMMA) {
			//found an argument
			result.push_back(Type::construct(str.substr(argStartIndex, i - argStartIndex)));
			argStartIndex = i + 1;
		}
		if (level == 0 && i == str.length() - 1) {
			// cout<<"arglist substr "<<str.substr(argStartIndex, i - argStartIndex)<<endl;
			result.push_back(Type::construct(str.substr(argStartIndex, i - argStartIndex)));
		}
	}
	// cout<<"arglist finished"<<endl;
	if (level != 0) {
		throw "err";
	}
	return result;
}


bool PrimType::isMe(string str) {
	return str == "int"
		|| str == "real"
		|| str == "str";
}
PrimType* PrimType::construct(string str) {
	//should not be call if same prim already constructed
	//do above in Type parsedTypeMap
	return new PrimType(str);
}
PrimType::PrimType(string str) {
	this->name = str;
}

bool ListType::isMe(string str) {
	if (str.length() < 4) {
		return false;
	}
	if (str[0] != '[') {
		return false;
	}
	if (!Type::isMe(str.substr(1, str.length() - 2))) {
		return false;
	}
	if (str[str.length() - 1] != ']') {
		return false;
	}
	return true;
}

ListType* ListType::construct(string str) {
	ListType* result = new ListType;
	result->parType = Type::construct(str.substr(1, str.length() - 2));
	return result;
}

void Unifier::unify(Type* type1, Type* type2) {
	if (type1 == nullptr || type2 == nullptr) {
		throw "BOTTOM";
	}
	//will return if inappropriate
	// cout<<"start constructEqualSet"<<endl;
	constructEqualSet(type1);
	constructEqualSet(type2);
	set<VarType*>* set1 = nullptr;
	set<VarType*>* set2 = nullptr;
	if (VarType* vt1 = dynamic_cast<VarType*>(type1)) {
		set1 = setMap[vt1];
	}
	if (VarType* vt2 = dynamic_cast<VarType*>(type2)) {
		set2 = setMap[vt2];
	}
	// cout<<"start bind"<<endl;
	if (set1 != nullptr && set2 != nullptr) {
		if (set1 != set2) {
			bind(set1, set2);
		}
		return;
	}
	if (set1 != nullptr && (FinalType::isMe(getType(type2)))) {
		bind(set1, dynamic_cast<FinalType*>(Type::construct(getType(type2))));
		return;
	}
	if (set2 != nullptr && (FinalType::isMe(getType(type1)))) {
		bind(set2, dynamic_cast<FinalType*>(Type::construct(getType(type1))));
		return;
	}

	if (ListType::isMe(getType(type1)) && ListType::isMe(getType(type2))) {
		ListType* listType1 = dynamic_cast<ListType*>(Type::construct(getType(type1)));
		ListType* listType2 = dynamic_cast<ListType*>(Type::construct(getType(type2)));
		unify(listType1->parType, listType2->parType);
		return;
	}
	if (FuncType::isMe(getType(type1)) && FuncType::isMe(getType(type2))) {
		FuncType* funcType1 = dynamic_cast<FuncType*>(Type::construct(getType(type1)));
		FuncType* funcType2 = dynamic_cast<FuncType*>(Type::construct(getType(type2)));
		if (funcType1->parTypes.size() != funcType2->parTypes.size()) {
			throw "Bottom";
		}
		for (int i = 0; i < funcType1->parTypes.size(); i++) {
			unify((funcType1->parTypes)[i], (funcType2->parTypes)[i]);
		}
		unify(funcType1->returnType, funcType2->returnType);
		return;
	}
	if (equals(type1, type2)) {
		return;
	}
	throw "Bottom";	
}

void Unifier::bind (set<VarType*>* set1, set<VarType*>* set2) {
	set1->insert(set2->begin(), set2->end());
	//TODO more test cases needed to check if this is right
	*set2 = *set1;

}
void Unifier::bind (set<VarType*>* set1, FinalType* finalType) {
	for (set<VarType*>::iterator typeIt = set1->begin(); typeIt != set1->end(); ++typeIt) {
		if (finalMap.find(*typeIt) != finalMap.end() && !equals(finalMap[*typeIt], finalType)) {
			throw "BOTTOM";
		}
		//update finalMap
		finalMap[*typeIt] = finalType;
		//update setMap
		setMap.erase(*typeIt);
	}
}
bool Unifier::equals (Type* type1, Type* type2) {
	return getType(type1) == getType(type2);
}
void Unifier::constructEqualSet (Type* type) {
	VarType* varType = dynamic_cast<VarType*>(type);
	if (!varType || setMap.find(varType) != setMap.end() || finalMap.find(varType) != finalMap.end()) {
		return;
	}
	set<VarType*>* newSet = new set<VarType*>;
	newSet->insert(varType);
	setMap[varType] = newSet;
	return;
}
string Unifier::getType(Type* type) {
	// cout<<"getting Type"<<endl;
	if (VarType* vt = dynamic_cast<VarType*>(type)) {
		if (gettedTypes.find(*vt) != gettedTypes.end()) {
			throw "BOTTOM";
		}
		gettedTypes.insert(*vt);
		string result = getTypeHelper(type);
		gettedTypes.erase(*vt);
		return result;	
	}
	return getTypeHelper(type);
}
string Unifier::getTypeHelper(Type* type) {
	if (VarType* vt = dynamic_cast<VarType*>(type)) {
		return getVarType(vt);
	}
	if (FuncType* vt = dynamic_cast<FuncType*>(type)) {
		return getFuncType(vt);
	}
	if (PrimType* vt = dynamic_cast<PrimType*>(type)) {
		return getPrimType(vt);
	}
	if (ListType* vt = dynamic_cast<ListType*>(type)) {
		return getListType(vt);
	}
	throw "BOTTOM";
}
string Unifier::getVarType(VarType* type) {
	if (finalMap.find(type) != finalMap.end()) {
		return getType(finalMap[type]);
	}
	if (setMap.find(type) != setMap.end()) {
		return "`" + (*(++(setMap[type]->begin())))->name;
	}
	// cout<<"typename "<< type->name;
	return "`" + type->name;
}
string Unifier::getPrimType(PrimType* type) {
	return type->name;
}
string Unifier::getListType(ListType* type) {
	return "[" + getType(type->parType) + "]";
}
string Unifier::getFuncType(FuncType* funcType) {
	string result = "(";
	vector<Type*> parTypes = funcType->parTypes;
	if (parTypes.size() != 0) {
		for (vector<Type*>::iterator typeIt = parTypes.begin(); typeIt != parTypes.end(); ++typeIt) {
			result.append(getType(*typeIt));
			result.append(",");
		}
	}
	if (result.length() > 1) {
		result[result.length() - 1] = ')';
	}
	else {
		result.append(")");
	}
	result += "->";
	result += getType(funcType->returnType);
	return result;
}

vector<std::string> &split(const string &s, char delim, vector<string> &elems) {
    stringstream ss(s);
    string item;
    while (std::getline(ss, item, delim)) {
        elems.push_back(item);
    }
    return elems;
}


vector<string> split(const string &s, char delim) {
    vector<string> elems;
    split(s, delim, elems);
    return elems;
}

int main()
{
	Unifier u = * new Unifier();
	while (true) {
		try {
			string input;
			getline(cin, input);
			if (input.length() == 0) {
				continue;
			}
			if (input == "QUIT") {
				return 0;
			}
			vector<string> types = split(input, '&');
			if (types.size() != 2) {
				throw "ERR";
			}
			Type* type1 = Type::construct(types[0]);
			Type* type2 = Type::construct(types[1]);
			u.unify(type1, type2);
			cout<<u.getType(type1)<<endl;
		}
		catch(const std::runtime_error& re)
		{
		    std::cerr << "Runtime error: " << re.what() << std::endl;
		}
		catch(const std::exception& ex)
		{
		    std::cerr << "Error occurred: " << ex.what() << std::endl;
		}
		catch(const char * e) {
			cout<<*e<<endl;
			return;
		}
	}
	return 0;
}
