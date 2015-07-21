package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class PythonLibStructureBuilder {
    
    private boolean isArchive;

    List<PythonClass> provideEntriesFromFile(final String path) {
        final List<PythonClass> pythonClasses = newArrayList();
        
        if (isPythonClass(path)) {
            pythonClasses.add(PythonClass.create(new File(path).getName()));
        } else {
            try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(path))) {
                ZipEntry entry = zipStream.getNextEntry();
                while (entry != null) {
                    if (isPythonClass(entry.getName())) {
                        pythonClasses.add(PythonClass.create(entry.getName()));
                    }
                    entry = zipStream.getNextEntry();
                }
                isArchive = true;
            } catch (final IOException e) {
                return pythonClasses;
            }
        }
        return pythonClasses;
    }

    private boolean isPythonClass(final String entryName) {
        return entryName.endsWith(".py") && !entryName.contains("__init__");
    }

    public boolean isArchive() {
        return isArchive;
    }

    static class PythonClass {
        private final String qualifiedName;

        private PythonClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        private static PythonClass create(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".py".length());
            final String qualifiedName = nameWithoutExtension.replaceAll("/", ".");
            return new PythonClass(qualifiedName);
        }

        String getQualifiedName() {
            return qualifiedName;
        }
    }
}
