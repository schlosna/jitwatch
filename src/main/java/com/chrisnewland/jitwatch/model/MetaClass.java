/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class MetaClass implements Comparable<MetaClass>
{
	private String className;
	private MetaPackage classPackage;

	private boolean isInterface = false;
	private boolean missingDef = false;

	private List<MetaMethod> classMethods = new CopyOnWriteArrayList<MetaMethod>();
	private List<MetaConstructor> classConstructors = new CopyOnWriteArrayList<MetaConstructor>();

	private int compiledMethodCount = 0;

	private Map<String, String> bytecodeCache = null;

	public MetaClass(MetaPackage classPackage, String className)
	{
		this.classPackage = classPackage;
		this.className = className;
	}

	public boolean isInterface()
	{
		return isInterface;
	}

	public void incCompiledMethodCount()
	{
		compiledMethodCount++;
	}

	public boolean hasCompiledMethods()
	{
		return compiledMethodCount > 0;
	}

	public void setInterface(boolean isInterface)
	{
		this.isInterface = isInterface;
	}

	public boolean isMissingDef()
	{
		return missingDef;
	}

	public void setMissingDef(boolean missingDef)
	{
		this.missingDef = missingDef;
	}

	public Map<String, String> getBytecodeCache(List<String> classLocations)
	{
		if (bytecodeCache == null)
		{
			bytecodeCache = BytecodeLoader.fetchByteCodeForClass(classLocations, getFullyQualifiedName());
		}

		return bytecodeCache;
	}

	public String toString2()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(classPackage.getName()).append(S_DOT).append(className).append(C_SPACE).append(compiledMethodCount)
				.append(S_SLASH).append(classMethods.size());

		return builder.toString();
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public String getName()
	{
		return className;
	}

	public String getFullyQualifiedName()
	{
		return classPackage.getName() + '.' + className;
	}

	public MetaPackage getPackage()
	{
		return classPackage;
	}

	public void addMetaMethod(MetaMethod method)
	{
		classMethods.add(method);
	}

	public void addMetaConstructor(MetaConstructor constructor)
	{
		classConstructors.add(constructor);
	}

	public List<IMetaMember> getMetaMembers()
	{
		List<IMetaMember> result = new ArrayList<>();

		IMetaMember[] constructorsArray = classConstructors.toArray(new MetaConstructor[classConstructors.size()]);
		Arrays.sort(constructorsArray);

		IMetaMember[] methodsArray = classMethods.toArray(new MetaMethod[classMethods.size()]);
		Arrays.sort(methodsArray);

		result.addAll(Arrays.asList(constructorsArray));
		result.addAll(Arrays.asList(methodsArray));

		return result;
	}

	public IMetaMember getMemberFromSignature(String name, String returnType, String[] paramTypes)
	{
		IMetaMember result = null;

		for (IMetaMember member : getMetaMembers())
		{
			//System.out.println("checking: " + member);
			
			if (memberMatches(member, name, returnType, paramTypes))
			{
				//System.out.println("match");
				result = member;
				break;
			}
		}

		return result;
	}

	private boolean memberMatches(IMetaMember member, String name, String returnType, String[] paramTypes)
	{
		boolean match = false;

		boolean nameMatch = member.getMemberName().equals(name);

		//System.out.println("name: " + member.getMemberName() + " / " + name);

		if (nameMatch)
		{
			boolean returnMatch = false;
			boolean paramsMatch = false;

			String memberReturnTypeName = member.getReturnTypeName();
			String[] memberArgumentTypeNames = member.getParamTypeNames();

			//System.out.println("return: " + memberReturnTypeName + " / " + returnType);

			if (memberReturnTypeName == null && returnType == null)
			{
				returnMatch = true;
			}
			else if (memberReturnTypeName != null && returnType != null && memberReturnTypeName.equals(returnType))
			{
				returnMatch = true;
			}

			if (memberArgumentTypeNames == null && paramTypes == null)
			{
				paramsMatch = true;
			}
			else if (memberArgumentTypeNames != null && paramTypes != null && memberArgumentTypeNames.length == paramTypes.length)
			{
				paramsMatch = true;

				for (int i = 0; i < memberArgumentTypeNames.length; i++)
				{
					String memberParam = memberArgumentTypeNames[i];
					String checkParam = paramTypes[i];

					//System.out.println("param: " + memberParam + " / " + checkParam);

					if (!memberParam.equals(checkParam))
					{
						paramsMatch = false;
						break;
					}
				}
			}

			//System.out.println("match: " + returnMatch + " / " + paramsMatch);

			match = returnMatch && paramsMatch;
		}

		return match;
	}

	@Override
	public int compareTo(MetaClass other)
	{
		return this.getName().compareTo(other.getName());
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		else
		{
			return toString().equals(obj.toString());
		}
	}
}
