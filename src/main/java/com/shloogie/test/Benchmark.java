package com.shloogie.test;

import java.time.LocalDate;
import java.util.function.Function;

import com.google.gson.Gson;
import com.shloogie.app.App;

public class Benchmark {
	public static void main(String[] args) throws Exception {
		Gson g = new Gson();
		App p = new App();

		Student s = new Student();
		s.id = 10;
		s.name = "Witek";
		s.b = new Book();
		s.b.author = "Olek Zdybel";
		s.b.title = "Jak zjebac dobrze dzialajace cos";
		s.b.noPages = 1;
		s.b.publishedOn = LocalDate.of(2017, 05, 18);
		s.b.s = new Student();
		s.b.s.name = "Jola";
		s.b.s.id = 50;
		s.b.s.b = new Book();
		s.b.s.b.author = "Jola Swawola";
		s.b.s.b.title = "Swawole";
		s.b.s.b.s = new Student();
		s.b.s.b.s.name = "Bosman";
		s.b.s.b.s.id = 666;
		s.b.s.b.s.b = new Book();
		s.b.s.b.s.b.author = "Bosman";
		s.b.s.b.s.b.title = "Moje wyklady sa zajebiste polecam 10/10";
		s.b.s.b.s.b.s = new Student();
		s.b.s.b.s.b.s.id = 5;
		s.b.s.b.s.b.s.b = new Book();
		s.b.s.b.s.b.s.b.author = "Admirał Bołt";
		s.b.s.b.s.b.s.b.title = "Zdane, brawo";
		s.b.s.b.s.b.s.b.positivelyReceived = true;
		/*s.b.s.b.s.b.s.b.s = new Student();
		s.b.s.b.s.b.s.b.s.id = 6;*/
				
		Book b = new Book();
		b.author = "Witold Bołt";
		b.title = "JVM Internals Handbook";
		b.noPages = 778;
		b.publishedOn = LocalDate.of(2017, 10, 01);
		b.positivelyReceived = true;
		b.checkCharacter = 'w';
		b.s = s;
		
		
		//System.out.println(p.toJson(s));
		//System.out.println(g.toJson(s));
		
		//System.out.println(p.toJson(b));
		//System.out.println(g.toJson(b));
		
		for(int i=0; i<10; i++) {
			compare(s, p, g);
			//compare(b, p, g);
		}
	}
	private static void compare(Object o, App j, Gson g) {
		long jitTime = measure(j::toJson, o);
		long googleTime = measure(g::toJson, o);
		System.out.println(o.getClass().getSimpleName() + " object: app = " + jitTime + " google = " + googleTime
				+ " ratio = " + (double) googleTime / jitTime);
	}

	private static long measure(Function<Object, String> converter, Object o) {
		converter.apply(o);
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < 500000; i++) {
			converter.apply(o);
		}
		return System.currentTimeMillis() - t1;
	}

}
