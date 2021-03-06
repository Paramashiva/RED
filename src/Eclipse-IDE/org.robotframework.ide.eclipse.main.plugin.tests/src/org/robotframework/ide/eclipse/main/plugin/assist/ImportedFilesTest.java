/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class ImportedFilesTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ImportedFilesTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir1");
        projectProvider.createDir("dir1_1");
        projectProvider.createDir("dir2");

        projectProvider.createFile("res.robot");
        projectProvider.createFile("dir1/res1.robot", "*** Variables ***");
        projectProvider.createFile("dir1_1/lib.py");
        projectProvider.createFile("dir1_1/vars.py");
        projectProvider.createFile("dir2/res2.robot", "*** Variables ***");
        projectProvider.createFile("dir2/tests.robot", "*** Test Cases ***");
    }

    @Test
    public void pythonFilesAreProperlyProvided() {
        final List<IFile> pythonFiles = ImportedFiles.getPythonFiles();
        assertThat(transform(pythonFiles, IFile::getName)).containsOnly("lib.py", "vars.py");
    }

    @Test
    public void resourceFilesAreProperlyProvided_1() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(projectProvider.getFile("res.robot"));
        assertThat(transform(resourceFiles, IFile::getName)).containsOnly("res1.robot", "res2.robot");
    }

    @Test
    public void resourceFilesAreProperlyProvided_2() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(projectProvider.getFile("dir1/res1.robot"));
        assertThat(transform(resourceFiles, IFile::getName)).containsOnly("res.robot", "res2.robot");
    }

    @Test
    public void resourceFilesAreProperlyProvided_3() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(projectProvider.getFile("dir2/res2.robot"));
        assertThat(transform(resourceFiles, IFile::getName)).containsOnly("res.robot", "res1.robot");
    }

    @Test
    public void pathsComparatorGivesPrecendenceForPathsInGivenProjectOverPathsFromDifferentProjects() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(projectProvider.getProject(), "");

        assertThat(compare(comparator, "/ImportedFilesTest/file.txt", "/Other/file.txt")).isNegative();
        assertThat(compare(comparator, "/Other/file.txt", "/ImportedFilesTest/file.txt")).isPositive();
    }

    @Test
    public void pathsComparatorGivesPrecendenceForShorterPathsInSameProject() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(projectProvider.getProject(), "");

        assertThat(compare(comparator, "/ImportedFilesTest/file.txt", "/ImportedFilesTest/dir/file.txt")).isNegative();
        assertThat(compare(comparator, "/ImportedFilesTest/dir/file.txt", "/ImportedFilesTest/file.txt")).isPositive();
    }

    @Test
    public void pathsComparatorGivesPrecendenceForPathWithSecondSegmentStartingFromPrefixInSameProject() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(projectProvider.getProject(), "xyz");

        assertThat(compare(comparator, "ImportedFilesTest/xyz_file.txt", "ImportedFilesTest/file_xyz.txt"))
                .isNegative();
        assertThat(compare(comparator, "ImportedFilesTest/file_xyz.txt", "ImportedFilesTest/xyz_file.txt"))
                .isPositive();
        assertThat(compare(comparator, "ImportedFilesTest/xyz/file.txt", "ImportedFilesTest/dir/file.txt"))
                .isNegative();
        assertThat(compare(comparator, "ImportedFilesTest/dir/file.txt", "ImportedFilesTest/xyz/file.txt"))
                .isPositive();
    }

    @Test
    public void pathsComparatorGivesPrecendenceForPathWhichFirstDifferentSegmentIsLexicographicallySmaller() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(projectProvider.getProject(), "");

        assertThat(compare(comparator, "/ImportedFilesTest/bc/d.txt", "/ImportedFilesTest/bd/d.txt")).isNegative();
        assertThat(compare(comparator, "/ImportedFilesTest/bd/d.txt", "/ImportedFilesTest/bc/d.txt")).isPositive();
    }

    @Test
    public void pathsComparatorReturnZeroForSamePaths() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(projectProvider.getProject(), "");

        assertThat(compare(comparator, "/ImportedFilesTest", "/ImportedFilesTest")).isZero();
        assertThat(compare(comparator, "/ImportedFilesTest/bc", "/ImportedFilesTest/bc")).isZero();
        assertThat(compare(comparator, "/ImportedFilesTest/bc/d.txt", "/ImportedFilesTest/bc/d.txt")).isZero();
    }

    private static int compare(final Comparator<IPath> comparator, final String p1, final String p2) {
        return comparator.compare(new Path(p1), new Path(p2));
    }
}
