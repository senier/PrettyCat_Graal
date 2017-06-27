package net.senier.prettycat;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.graalvm.compiler.api.test.Graal;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.util.Providers;
import org.graalvm.compiler.runtime.RuntimeProvider;

import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public class GraalExport {
	
	static MetaAccessProvider metaAccess;

	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("Invalid arguments");
			return;
		}

		String jarfilename = new String (args[0]);
		String classname   = new String (args[1]);
		File jarfile       = new File(jarfilename);

		RuntimeProvider rt   = Graal.getRequiredCapability(RuntimeProvider.class);
		Providers providers  = rt.getHostBackend().getProviders();
		metaAccess           = providers.getMetaAccess();
		OptionValues options = Graal.getRequiredCapability(OptionValues.class);

		try {

			URLClassLoader urlCl = new URLClassLoader(new URL[] { jarfile.toURI().toURL()},System.class.getClassLoader());
			Class cl = urlCl.loadClass(classname);
			for (Method m : cl.getDeclaredMethods()) {
				ResolvedJavaMethod method = metaAccess.lookupJavaMethod(m);
				StructuredGraph graph = new StructuredGraph.Builder(options, AllowAssumptions.YES).method(method).build();
				System.out.println(m.getName());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Class '" + classname + "' not found in '" + jarfilename + "'");
			e.printStackTrace();
		}
	}

}