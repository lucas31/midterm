package com.shloogie.app;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class App 
{
	private ClassPool pool;
	private Map<Class<?>, Converter> cache;
	private Map<String, Integer> c2;
		
	public App() {
		pool = ClassPool.getDefault();
		cache = new HashMap<Class<?>, Converter>();
		c2 = new HashMap<String, Integer>();
	}
	
	public static String bp = "\"";
	public static String p = "\\\"";
	public static String pl = "+";
	public static String ppl = p+pl;
	public static String c = ",";
	public static String pcp = p+c+p;
	public static String bpcbp = bp+c+bp;
	public static String opc = "[";
	public static String cpc = "]";
	
	public static String toProperJSON(String string) {	
		/*String result = "" + string.toCharArray()[0];
		
		for(int i=1; i<string.length() - 1; i++) {
			if(string.toCharArray()[i] == ',' || string.toCharArray()[i-1] == ',') result += "\"";
			if (string.toCharArray()[i] == ']' || string.toCharArray()[i-1] == '[') result += "\"";
			if (string.toCharArray()[i] != ' ' && string.toCharArray()[i+1] != '[') result += string.toCharArray()[i];
			
			if (string.toCharArray()[i-1] != ']') result += string.toCharArray()[i];	
		}
		
		return result + string.toCharArray()[string.length() - 1];*/
		
		String result = string;

		/*result = result.replaceAll(", ", "\",\"");
		result = result.replaceAll("[\\[]", "[\"");
		result = result.replaceAll("[\\]]", "\"]");
				*/
		return result;
	}
	
	public static String WrapIn(String string, String wrapIn) {
		return wrapIn + string + wrapIn;
	}
		
	public static boolean IsNanField(Field f) {
		return !(f.getGenericType().toString().contains("int") || 
				f.getGenericType().toString().contains("float") || 
				f.getGenericType().toString().contains("double") || 
				f.getGenericType().toString().contains("short") || 
				f.getGenericType().toString().contains("long") || 
				f.getGenericType().toString().contains("bool") ||
				f.getGenericType().toString().contains("Bool"));
	}
	
	public static String WrapNanFields(Field f, boolean useGetter, String prefix){
		boolean wrap = IsNanField(f);
		
		return (wrap ? bp + p + bp + pl : "") + 
				//(useGetter ? prefix + "getClass().getDeclaredField(\"" + f.getName() + "\").get(" + prefix.substring(0, prefix.length() - 1) + ")" : 
					//"(" + prefix.substring(0, prefix.length() - 1) + " != null ? " + prefix + f.getName() + " : \"\\\"null\\\"\")"
					//prefix + f.getName()
					//) +
				prefix + f.getName() +
				(wrap ? pl + bp + p + bp : "");	
	}
	
	/*public static String JSONizeArray(Field f) {
		String result = "\"[\"for(int i=0; i<o." + f.getName() + ".size(); i++) "\""
				+ o[i].toString() + "\""
			
			if (i < o. .f.getName() .size() - 1) result += ",";
		}
		
		
		return result + "\"]\"";		
	}*/

	public static String JSONizeField(Field f, boolean addComma, boolean nameOnly, String prefix) {
		/*String res = "\"if(o.getClass().getField(\\\"" + f.getName() + "\\\").isAccessible()) { \"+";
		res += (addComma ? bp+c+bp+pl : "")+bp+p+f.getName()+p+":"+bp+(nameOnly ? "" : pl+WrapNanFields(f, false));
		res += "+\"} else { \"+";
		res += (addComma ? bp+c+bp+pl : "")+bp+p+f.getName()+p+":"+bp+(nameOnly ? "" : pl+WrapNanFields(f, true));
		res += "+\"}\"";*/
		return (addComma ? bp+c+bp+pl : "")+bp+p+f.getName()+p+":"+bp+(nameOnly ? "" : pl+WrapNanFields(f, false, prefix));	
	}
	
	private int CountChar(String string, String toFind) {
		if(c2.get(toFind) == null) c2.put(toFind, 1);
		else c2.put(toFind, c2.get(toFind)+1);
		return c2.get(toFind);
	}
	
	private String getConverterMethodBody(Class<?> cls, boolean inside, String adn) throws  SecurityException, NoSuchFieldException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		StringBuilder sb = new StringBuilder();//"{");
		if(!inside) sb = new StringBuilder("public String toJson(" + cls.getName() + " o) throws SecurityException, NoSuchFieldException { String result = \"{\";\n");
	
		String prefix = "o.";
		if(inside) prefix = "o." + adn;
		
		for(int i=0; i<cls.getDeclaredFields().length; i++) {
			Field fld = cls.getDeclaredFields()[i];
			String type = fld.getType().toString();

			
			
			if (type.contains("class")) sb.append("if(" + prefix + fld.getName() + " != null) {");
			//sb.append("if(" + prefix.substring(0, prefix.length() - 1) + " != null) " + prefix + "getClass().getDeclaredField(\"" + fld.getName() + "\").setAccessible(true);\n");
			if(type.contains("Array")) {			
				sb.append("result += " + JSONizeField(fld, i!=0, true, prefix) + "+\"[\";");
				sb.append("for(int i=0; i<" + prefix + fld.getName() + ".size();i++) "
						+ "result += (i!=0 ? \",\" : \"\") + \"\\\"\" + " + prefix + fld.getName() + ".get(i) + \"\\\"\" ;");
				
				sb.append("result += \"]\";\n");
			}
			else if (type.contains("char")) {
				sb.append("result += " + JSONizeField(fld, i!=0, true, prefix) + ";\n");
				
				sb.append("if ((int)" + prefix + fld.getName() + " == 0) result += \"\\\"\\\\u0000\\\"\";\n" +
						  "else result += \"\\\"\"+" + prefix + fld.getName() + "+\"\\\"\";\n");
			}
			else if(type.contains("class") && !type.contains("java.")) {
				//if(CountChar(prefix, fld.getName()) <= 4 ) {
				if(prefix.length() - prefix.replace(fld.getName(), "").length() <= 4) {
				
				Class<?> clazz = Class.forName(fld.getType().getName());
				sb.append("result += " + JSONizeField(fld, i!=0, true, prefix) + "+\"{\";\n");
				sb.append(getConverterMethodBody(clazz, true, adn+fld.getName() + "."));
				sb.append("result += \"}\";\n");
				
				}
				//System.out.println("types " + fld.getType().getName());
				//Class<?> clazz = Class.forName(fld.getType().getName());
				//System.out.println("printing " + getConverterMethodBody(clazz, true, adn+fld.getName() + "."));
				//System.out.println(field.get(Class.forName(field.getType().toString().replaceAll("class ", ""))));
			}
			else if(type.contains("Date")) {
				sb.append("result += " + JSONizeField(fld, i!=0, true, prefix) + "+\"{\"");
				sb.append(translateDate(fld, prefix+fld.getName()+"."));
				//String ans = translateDate(fld, "o."+adn+fld.getName()+".");
			}
			else sb.append("result += " + JSONizeField(fld, i!=0, false, prefix) + ";\n");
				 //sb.append("result += \"" + fld.getName() + ":\"+" + prefix + fld.getName() + ";");

			if (type.contains("class")) sb.append("}");
		}
		if (!inside) sb.append("return result + \"}\";}");
		//else sb.append("}");
		
		//System.out.println(sb.toString());
		
		return sb.toString();
	}
	
	private String translateDate(Field field, String before) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(field.getType().getName());

		String yearMethod="";
		String dayMethod="";
		String monthMethod="";
		for (Method m : clazz.getMethods()) {
			String mm = m.getName().toLowerCase();
			if(mm.contains("get")) {
				
				if(mm.contains("year") && !mm.contains("day"))
				yearMethod = m.getName();
				if(mm.contains("month")) {
					if(mm.contains("day")){
						dayMethod = m.getName();
					} else if (m.getReturnType().toString().contains("int")){
						monthMethod = m.getName();
					}
				}
			}
		}
		//System.out.println(before + yearMethod + "()\n" + before + monthMethod + "()\n" + before + dayMethod+"()");
		return "+\"\\\"year\\\":\"+" + (before + yearMethod) + "()+" +
			   "\",\\\"month\\\":\"+((int)" + (before + monthMethod) + "())+" +
			   "\",\\\"day\\\":\"+" + (before + dayMethod) + "()+" + "\"}\";\n";
	
		//"publishedOn":{"year":2017,"month":10,"day":1}
		
	}
	
	/*public static String getConverterMethodBody(Class<?> cls) {
		StringBuilder sb = new StringBuilder("public String toJson(" + cls.getName() + " o) { return \"{\"+\"");

		for(int i=0; i<cls.getDeclaredFields().length; i++){
			
			Field fld = cls.getDeclaredFields()[i];			
			
			//if(fld.getType().getTypeName().contains("Array")) {
				
			//}
			//else
				sb.append(JSONizeField(fld, fld.getType().getTypeName().contains("Array")));
			
			
			
			if(i < cls.getDeclaredFields().length - 1) sb.append(pl + bpcbp + pl + bp);
		}
		
		//for(Field f : cls.getFields()) sb.append(JSONizeField(f));
		
		sb.append("+\"}\"; }");
		
		//System.out.println(sb.toString());//.replaceAll("\\\\\"", " "));
		
		return sb.toString();
	}*/

	public String toJson(Object o) {
		try {
			// warning: this is not thread safe!			
			if (!cache.containsKey(o.getClass())) {
				cache.put(o.getClass(), getConverter(o.getClass()));
			}
			return cache.get(o.getClass()).toJson(o);
		} catch (Exception e) {
			//e.printStackTrace();
			return e.getMessage();// + " | " + e.getCause();
		}
	}
	
	/*public static String GetField(Object o, String field) {
		try {
			Field f = o.getClass().getDeclaredField(field);
			
			f.setAccessible(true);
			if(f.getType().toString().contains("Array")) return toProperJSON(f.get(o).toString());			
			return f.get(o).toString();
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}*/
	
	private Converter getConverter(Class<?> cls)
			throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException {

		// new class with a random name, as this name is not needed in any way
		CtClass converterClass = pool.makeClass(UUID.randomUUID().toString());

		/*for(Field fld : cls.getDeclaredFields()) {
			if(!fld.isAccessible()) {
				converterClass.addMethod(CtNewMethod.make(
					"public " + fld.getType().getSimpleName() + " Get" + fld.getName() + "() {" +
					"return this." + fld.getName() + "; }"
					, converterClass));
			}
		}*/
		
		converterClass.addMethod(CtNewMethod.make(getConverterMethodBody(cls, false, ""), converterClass));
		converterClass.addMethod(CtNewMethod
				.make("public String toJson(Object o){return toJson((" + cls.getName() + ")o);}", converterClass));
		//converterClass.addConstructor(CtNewConstructor.make("public" + cls.getSimpleName() + "(){}", converterClass));
		converterClass.setInterfaces(new CtClass[] { pool.get("com.shloogie.app.Converter") });
				
		Converter result = (Converter) pool.toClass(converterClass).newInstance();
				
		// this allows us to save memory
		converterClass.detach();
		return result;
	}
}
