#include <iostream>
#include <stdlib.h>
#include <sstream>
#include <string>
#include <map>
#include <set>
#include <vector>
#include <ctype.h>
#include <algorithm>
#include <exception>

using namespace std;

class Type
{
private:
	//hold the value of Type actually
	static map<string, Type*> parsedTypeMap;

public:
	static Type* construct(string str);
	virtual void foo() {}
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
	virtual void bar() {}
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
	set<string> gettedTypes;
	void bind(set<VarType*>* set1, set<VarType*>* set2);
	void bind(set<VarType*>* set1, FinalType* finalType);
	bool equals(Type* type1, Type* type2);
	set<VarType*>* constructEqualSet(Type* type);
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
	if (!isalpha(str[1])) {
		return false;
	}
	for (int i = 2; i < str.length(); i++) {
		if (!isalnum(str[i])) {
			return false;
		}
	}
	return true;
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
	catch(const exception& ex)
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
		throw "ERR";
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
		throw "ERR";
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
		throw "ERR";
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
		if (level == 0) {
			if (i == str.length() - 1) {
				// cout<<"arglist substr "<<str.substr(argStartIndex, i - argStartIndex)<<endl;
				result.push_back(Type::construct(str.substr(argStartIndex, i - argStartIndex)));
			}
			else {
				throw "ERR";
			}
		}
	}
	// cout<<"arglist finished"<<endl;
	if (level != 0) {
		throw "ERR";
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
	//will return if inappropriate

	set<VarType*>* set1 = constructEqualSet(type1);
	set<VarType*>* set2 = constructEqualSet(type2);
	if (!set1->empty()&& !set2->empty()) {
		// cout<<"set1 & set2 not empty"<<endl;
		if (*set1 != *set2) {
			// cout<<"start bind 2 sets unify"<<endl;
			bind(set1, set2);
		}
		return;
	}
	if (!set1->empty() && (FinalType::isMe(getType(type2)))) {
		// cout<<"start bind finalType in unify"<<endl;
		bind(set1, dynamic_cast<FinalType*>(Type::construct(getType(type2))));
		return;
	}
	if (!set2->empty() && (FinalType::isMe(getType(type1)))) {
		// cout<<"start bind finalType in unify"<<endl;
		bind(set2, dynamic_cast<FinalType*>(Type::construct(getType(type1))));
		return;
	}
	if (ListType::isMe(getType(type1)) && ListType::isMe(getType(type2))) {
		// cout<<"start bind finalType in unify"<<endl;
		ListType* listType1 = dynamic_cast<ListType*>(Type::construct(getType(type1)));
		ListType* listType2 = dynamic_cast<ListType*>(Type::construct(getType(type2)));
		unify(listType1->parType, listType2->parType);
		return;
	}

	if (FuncType::isMe(getType(type1)) && FuncType::isMe(getType(type2))) {
		// cout<<"start bind FuncType in unify"<<endl;
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
	// cout<<"......................set1 size"<<set1->size()<<endl;
	//TODO more test cases needed to check if this is right
	for (set<VarType*>::iterator typeIt = set2->begin(); typeIt != set2->end(); typeIt++) {
		setMap[*typeIt] = set1;
	}

}
void Unifier::bind (set<VarType*>* set1, FinalType* finalType) {
	// cout<<"start bind finalType"<<endl;
	for (set<VarType*>::iterator typeIt = set1->begin(); typeIt != set1->end(); typeIt++) {
		if (finalMap.find(*typeIt) != finalMap.end() && !equals(finalMap[*typeIt], finalType)) {
			throw "BOTTOM";
		}
		// cout<<"start update finalMap"<<endl;
		//update finalMap
		finalMap[*typeIt] = finalType;
		// cout<<"start update setMap"<<endl;
		//update setMap
		setMap.erase(*typeIt);
	}
}
bool Unifier::equals (Type* type1, Type* type2) {
	return getType(type1) == getType(type2);
}
//construct a new equal set for new vartype, or return existing equal set
set<VarType*>* Unifier::constructEqualSet (Type* type) {
	// cout<<"begin constructEqualSet"<<endl;
	VarType* varType = dynamic_cast<VarType*>(type);
	set<VarType*>* newSet = new set<VarType*>();
	if (!varType || finalMap.find(varType) != finalMap.end()) {
		return newSet;
	}
	if (setMap.find(varType) != setMap.end()) {
		return setMap[varType];
	}
	// cout<<"start constructEqualSet for "<<varType->name<<endl;
	newSet->insert(varType);
	setMap[varType] = newSet;
	return newSet;
}
string Unifier::getType(Type* type) {
	if (VarType* vt = dynamic_cast<VarType*>(type)) {
		// cout<<"getting VarType "<<vt->name<<endl;
		if (gettedTypes.find(vt->name) != gettedTypes.end()) {
			throw "BOTTOM";
		}
		// cout<<"gettedTypes size: before insert"<<gettedTypes.size()<<endl;
		gettedTypes.insert(vt->name);
		string result = getTypeHelper(type);
		gettedTypes.erase(vt->name);
		// cout<<"gettedTypes size: after erase"<<gettedTypes.size()<<endl;
		return result;
	}
	return getTypeHelper(type);
}
string Unifier::getTypeHelper(Type* type) {
	if (VarType* vt = dynamic_cast<VarType*>(type)) {
		// cout<<"getting VarType "<<vt->name<<endl;
		return getVarType(vt);
	}
	if (FuncType* vt = dynamic_cast<FuncType*>(type)) {
		// cout<<"getting FuncType "<<endl;
		return getFuncType(vt);
	}
	if (PrimType* vt = dynamic_cast<PrimType*>(type)) {
		// cout<<"getting primType "<<vt->name<<endl;
		return getPrimType(vt);
	}
	if (ListType* vt = dynamic_cast<ListType*>(type)) {
		// cout<<"getting ListType "<<endl;
		return getListType(vt);
	}
	throw "BOTTOM";
}
string Unifier::getVarType(VarType* type) {
	if (finalMap.find(type) != finalMap.end()) {
		return getType(finalMap[type]);
	}
	if (setMap.find(type) != setMap.end()) {
		// cout<<"gettingVarType and found in setMap and set size is "<<setMap[type]->size()<<endl;
		return "`" + (*(setMap[type]->begin()))->name;
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

vector<string> split(const string &s, char delim) {
	vector<string> elems;
    stringstream ss(s);
    string item;
    while (getline(ss, item, delim)) {
        elems.push_back(item);
    }
    return elems;
}

bool isSpaceOrTab(char a) {
	return a == ' ' || a == '\t';
}

string eliminateSpaces(string &str) {
	//check for space
	bool flag = false;
	for (int i = 0; i < str.length(); i++) {
		if (flag && isalnum(str[i])) {
			if (isSpaceOrTab(str[i - 1])) {
				throw "ERR";
			}
		}
		if (isalnum(str[i])) {
			flag = true;
			continue;
		}
		if (!isSpaceOrTab(str[i])) {
			flag = false;
		}
	}
	for (int i = 0; i < str.length(); i++) {
		if (str[i] == '`') {
			if (i == str.length() - 1 || isSpaceOrTab(str[i + 1])) {
				throw "ERR";
			}
		}
	}
	str.erase(remove(str.begin(), str.end(), ' '),str.end());
	str.erase(remove(str.begin(), str.end(), '\t'),str.end());
	return str;
}

int main()
{
	Unifier u = * new Unifier();
	while (true) {
		try {
			string rawInput;
			getline(cin, rawInput);
			string input = eliminateSpaces(rawInput);
			if (input.length() == 0) {
				continue;
			}
			if (input == "QUIT") {
				return 0;
			}
			vector<string> types = split(input, '&');
			if (types.size() != 2 || input[input.length() - 1] == '&') {
				throw "ERR";
			}
			Type* type1 = Type::construct(types[0]);
			Type* type2 = Type::construct(types[1]);
			u.unify(type1, type2);
			// cout<<"finished unify"<<endl;
			cout<<u.getType(type1)<<endl;
		}
		catch(const exception& ex)
		{
		    cout<<"BOTTOM"<<endl;
		}
		catch(const char * e) {
			if (*e == 'E') {
				cout<<"ERR"<<endl;
			}
			else {
				cout<<"BOTTOM"<<endl;
			}
			return 0;
		}
	}
	return 0;
}


