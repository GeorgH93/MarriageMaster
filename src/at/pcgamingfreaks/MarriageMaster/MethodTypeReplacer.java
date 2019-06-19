/*
 *   Copyright (C) 2019 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.InputStream;

import static org.objectweb.asm.Opcodes.ASM4;

public class MethodTypeReplacer extends ClassVisitor
{
	private final String originalParameterType, newParameterType;

	public MethodTypeReplacer(String originalParameterType, String newParameterType, ClassVisitor cv)
	{
		super(ASM4, cv);
		this.originalParameterType = originalParameterType;
		this.newParameterType = newParameterType;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		return super.visitMethod(access, name, (desc.contains(originalParameterType)) ? desc.replaceAll(originalParameterType, newParameterType) : desc, signature, exceptions);
	}

	public static byte[] replace(String originalParameterType, String newParameterType, InputStream classStream) throws Exception
	{
		ClassReader reader = new ClassReader(classStream);
		ClassWriter writer = new ClassWriter(reader, 0);

		MethodTypeReplacer replacer = new MethodTypeReplacer(originalParameterType, newParameterType, writer);
		reader.accept(replacer, 0);
		return writer.toByteArray();
	}
}