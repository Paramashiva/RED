/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CopySettingsHandler.E4CopySettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopySettingsHandler extends DIParameterizedHandler<E4CopySettingsHandler> {

    public CopySettingsHandler() {
        super(E4CopySettingsHandler.class);
    }

    public static class E4CopySettingsHandler {

        @Execute
        public void copySettings(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {
            final List<RobotSetting> settings = Selections.getElements(selection, RobotSetting.class);
            if (!settings.isEmpty()) {
                
                final Object settingsCopy = TableHandlersSupport.createSettingsCopy(settings);
                clipboard.insertContent(settingsCopy);
            }
        }
    }
}
