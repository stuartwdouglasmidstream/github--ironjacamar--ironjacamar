/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jca.codegenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Code generator main class
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class Main
{
   private static final int OTHER = 2;
   
   /**
    * Code generator standalone tool
    * 
    * @param args command line arguments
    */
   public static void main(String[] args)
   {
      String outputDir = "out"; //default output directory
      int arg = 0;
 
      if (args.length > 0)
      {
         while (args.length > arg + 1)
         {
            if (args[arg].startsWith("-"))
            {
               if (args[arg].equals("-o"))
               {
                  arg++;
                  if (arg >= args.length)
                  {
                     usage();
                     System.exit(OTHER);
                  }
                  outputDir = args[arg];
               }
            } 
            else
            {
               usage();
               System.exit(OTHER);
            }
            arg++;
         }
      }
      try 
      {
         ResourceBundle dbconf = ResourceBundle.getBundle("codegenerator", Locale.getDefault());

         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         System.out.print(dbconf.getString("package.name"));
         String packageName = in.readLine();
         System.out.print(dbconf.getString("class.name"));
         String className = in.readLine();
         

         Template template = new SimpleTemplate();
         Definition def = new Definition();
         def.setRaPackage(packageName);
         def.setRaClass(className);
         
         List<ConfigPropType> props = new ArrayList<ConfigPropType>();
         while (true)
         {
            System.out.println(dbconf.getString("config.properties"));
            System.out.print("    " + dbconf.getString("config.properties.name"));
            String name = in.readLine();
            if (name == null || name.equals(""))
               break;
            System.out.print("    " + dbconf.getString("config.properties.type"));
            String type = in.readLine();
            System.out.print("    " + dbconf.getString("config.properties.value"));
            String value = in.readLine();
            System.out.println();
            
            ConfigPropType config = new ConfigPropType(name, type, value);
            props.add(config);
         }
         def.setRaConfigProps(props);

         FileWriter fw = createSrcFile(className + ".java", packageName, outputDir);
         template.process(def, fw);
         fw.close();
         
         //ant build.xml
         FileWriter antfw = createFile("build.xml", outputDir);
         URL headerFile = SimpleTemplate.class.getResource("/build.xml.template");
         String headerString = Utils.readFileIntoString(headerFile);
         antfw.write(headerString);
         antfw.close();
         
         System.out.println(dbconf.getString("java.wrote"));
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Create source file
    * @param name The name of the class
    * @param packageName The package name
    * @param outDir output directory
    * @return The file
    * @exception IOException Thrown if an error occurs 
    */
   private static FileWriter createSrcFile(String name, String packageName, String outDir) throws IOException
   {
      String directory = "src";

      if (packageName != null && !packageName.trim().equals(""))
      {
         directory = directory + File.separatorChar +
                     packageName.replace('.', File.separatorChar);
      }

      File path = new File(outDir, directory);
      if (!path.exists())
         path.mkdirs();
      
      File file = new File(path.getAbsolutePath() + File.separatorChar + name);

      if (file.exists())
         file.delete();

      return new FileWriter(file);
   }
   
   /**
    * Create file
    * @param name The name of the class
    * @param outDir output directory
    * @return The file
    * @exception IOException Thrown if an error occurs 
    */
   private static FileWriter createFile(String name, String outDir) throws IOException
   {
      File path = new File(outDir);
      if (!path.exists())
         path.mkdirs();
      
      File file = new File(path.getAbsolutePath() + File.separatorChar + name);

      if (file.exists())
         file.delete();

      return new FileWriter(file);
   }
   
   /**
    * Tool usage
    */
   private static void usage()
   {
      System.out.println("Usage: codegenerator [-o directory]");
   }

}