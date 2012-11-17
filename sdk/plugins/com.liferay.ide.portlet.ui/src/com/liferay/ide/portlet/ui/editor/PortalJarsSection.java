/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.liferay.ide.portlet.ui.editor;

import com.liferay.ide.core.model.IBaseModel;
import com.liferay.ide.core.model.IModelChangedEvent;
import com.liferay.ide.core.model.IModelChangedListener;
import com.liferay.ide.portlet.core.IPluginPackageModel;
import com.liferay.ide.portlet.core.PluginPackageModel;
import com.liferay.ide.portlet.ui.LangMessages;
import com.liferay.ide.ui.form.DefaultContentProvider;
import com.liferay.ide.ui.form.FormLayoutFactory;
import com.liferay.ide.ui.form.IDEFormPage;
import com.liferay.ide.ui.form.TablePart;
import com.liferay.ide.ui.form.TableSection;
import com.liferay.ide.ui.wizard.ExternalFileSelectionDialog;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class PortalJarsSection extends TableSection implements IModelChangedListener, IPropertyChangeListener {

	private static final int ADD_INDEX = 0;
	private static final int REMOVE_INDEX = 1;
	private static final int UP_INDEX = 2;
	private static final int DOWN_INDEX = 3;

	private TableViewer fViewer;
	private Vector<File> fJars;
	private Action fAddAction;
	private Action fRemoveAction;

	// private Action fSortAction;

	class PortalJarsContentProvider extends DefaultContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			if (fJars == null) {
				createJarsArray();
			}
			return fJars.toArray();
		}
	}
	
	protected void createJarsArray() {
		fJars = new Vector<File>();
		PluginPackageModel model = (PluginPackageModel) getPage().getModel();
		String[] portalJars = model.getPortalDependencyJars();
		IPath portalDir = ((PluginPackageEditor)getPage().getEditor()).getPortalDir();

        if( portalDir != null )
        {
            for (String portalJar : portalJars) {
                File jarFile = new File(portalDir.append("WEB-INF/lib").toFile(), portalJar.trim()); //$NON-NLS-1$

                if (jarFile.isFile() && jarFile.exists()) {
                    fJars.add(jarFile);
                }
            }
        }
		
	}
	
	class PortalJarsLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR);
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof File) {
				File file = (File)element;
				return file.getName();
			}
			return ""; //$NON-NLS-1$
		}
		
	}

	public PortalJarsSection(IDEFormPage page, Composite parent, String[] labels) {
		super(page, parent, Section.DESCRIPTION, labels);
		getSection().setText(LangMessages.PortalJarsSection_portal_dependency_jars);
		getSection().setDescription(LangMessages.PortalJarsSection_specify_which_jars_the_plugin_package_requires);
		getSection().getTextClient().getParent().layout(true);
		getTablePart().setEditable(true);
	}

	public void createClient(Section section, FormToolkit toolkit) {
		Composite container = createClientContainer(section, 2, toolkit);
		createViewerPartControl(container, SWT.MULTI, 2, toolkit);
		TablePart tablePart = getTablePart();
		fViewer = tablePart.getTableViewer();

		fViewer.setContentProvider(new PortalJarsContentProvider());
		fViewer.setLabelProvider(new PortalJarsLabelProvider());
		toolkit.paintBordersFor(container);
		makeActions();
		section.setClient(container);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumWidth = 250;
		gd.grabExcessVerticalSpace = true;
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(gd);
		section.setText(LangMessages.PortalJarsSection_portal_dependency_jars);
		createSectionToolbar(section, toolkit);
		initialize();
	}

	private void createSectionToolbar(Section section, FormToolkit toolkit) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});

		// Add sort action to the tool bar
		// fSortAction = new SortAction(fViewer, "Sort alphabetically", null, null, this);
		// toolBarManager.add(fSortAction);

		toolBarManager.update(true);

		section.setTextClient(toolbar);
	}

	protected void selectionChanged(IStructuredSelection sel) {
		getPage().getFormEditor().setSelection(sel);
		updateButtons();
	}

	private void updateButtons() {
		Table table = getTablePart().getTableViewer().getTable();
		TableItem[] selection = table.getSelection();
		boolean hasSelection = selection.length > 0;
		TablePart tablePart = getTablePart();
		tablePart.setButtonEnabled(ADD_INDEX, isEditable());
		updateUpDownButtons();
		tablePart.setButtonEnabled(REMOVE_INDEX, isEditable() && hasSelection);
	}

	private void updateUpDownButtons() {
		TablePart tablePart = getTablePart();
		// if (fSortAction.isChecked()) {
		// tablePart.setButtonEnabled(UP_INDEX, false);
		// tablePart.setButtonEnabled(DOWN_INDEX, false);
		// return;
		// }
		Table table = getTablePart().getTableViewer().getTable();
		TableItem[] selection = table.getSelection();
		boolean hasSelection = selection.length > 0;
		boolean canMove = table.getItemCount() > 1 && selection.length == 1;

		tablePart.setButtonEnabled(UP_INDEX, canMove && isEditable() && hasSelection && table.getSelectionIndex() > 0);
		tablePart.setButtonEnabled(DOWN_INDEX, canMove && hasSelection && isEditable() && table.getSelectionIndex() < table.getItemCount() - 1);
	}

	protected void handleDoubleClick(IStructuredSelection sel) {
	}

	protected void buttonSelected(int index) {
		switch (index) {
			case ADD_INDEX :
				handleAdd();
				break;
			case REMOVE_INDEX :
				handleRemove();
				break;
			case UP_INDEX :
				handleUp();
				break;
			case DOWN_INDEX :
				handleDown();
				break;
		}
	}


	public void dispose() {
		
		IBaseModel model =  getPage().getModel();
		if (model != null) {
			model.dispose();
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDESection#doGlobalAction(java.lang.String)
	 */
	public boolean doGlobalAction(String actionId) {

		if (!isEditable()) {
			return false;
		}

		if (actionId.equals(ActionFactory.DELETE.getId())) {
			handleRemove();
			return true;
		}
		if (actionId.equals(ActionFactory.CUT.getId())) {
			// delete here and let the editor transfer
			// the selection to the clipboard
			handleRemove();
			return false;
		}
		if (actionId.equals(ActionFactory.PASTE.getId())) {
			doPaste();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#canPaste(java.lang.Object, java.lang.Object[])
	 */
	protected boolean canPaste(Object targetObject, Object[] sourceObjects) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#doPaste(java.lang.Object, java.lang.Object[])
	 */
	protected void doPaste(Object targetObject, Object[] sourceObjects) {
		// Get the model
		
	}

	public boolean setFormInput(Object object) {
//		if (object instanceof IPluginImport) {
//			ImportObject iobj = new ImportObject((IPluginImport) object);
//			fImportViewer.setSelection(new StructuredSelection(iobj), true);
//			return true;
//		}
		return false;
	}

	protected void fillContextMenu(IMenuManager manager) {
		
	}

	@SuppressWarnings("rawtypes")
	private void handleRemove() {
		IStructuredSelection ssel = (IStructuredSelection) fViewer.getSelection();
		PluginPackageModel model = (PluginPackageModel)getPage().getModel();
		int i = 0;
		String[] removedFiles = new String[ssel.size()];
		for (Iterator iter = ssel.iterator(); iter.hasNext(); i++) {
			removedFiles[i] = ((File)iter.next()).getName();
		}
		
		model.removePortalDependencyJars(removedFiles);
		updateButtons();
	}

	private void handleAdd() {
		PluginPackageModel model = (PluginPackageModel) getPage().getModel();
		String[] existingJars = model.getPortalDependencyJars();
		PluginPackageEditor editor = (PluginPackageEditor)getPage().getEditor();
		IPath portalDir = editor.getPortalDir();
        
		if( portalDir != null )
		{
            ExternalFileSelectionDialog dialog =
                new ExternalFileSelectionDialog( getPage().getShell(), new PortalJarViewerFilter(
                    portalDir.toFile(), new String[] { "WEB-INF", "WEB-INF/lib" }, existingJars ), true, false ); //$NON-NLS-1$ //$NON-NLS-2$
            dialog.setInput(portalDir.toFile());
	        dialog.create();
	        if (dialog.open() == Window.OK) {
	            Object[] selectedFiles = dialog.getResult();
	            try {
	                for (int i = 0; i < selectedFiles.length; i++) {
	                    File jar = (File) selectedFiles[i];
	                    if (jar.exists()) {
	                        model.addPortalDependencyJar(jar.getName());
	                    }
	                }
	            } catch (Exception e) {
	            }
	        }
		}
        else
        {
            MessageDialog.openInformation(
                getPage().getShell(), LangMessages.PortalJarsSection_liferay_plugin_package_editor,
                LangMessages.PortalJarsSection_can_not_determine_portal_direcory_make_sure_liferay_portal_is_set_as_the_targeted_runtime );
        }
	}
	
	private void handleUp() {
		int index = getTablePart().getTableViewer().getTable().getSelectionIndex();
		if (index < 1)
			return;
		swap(index, index - 1);
	}

	private void handleDown() {
		Table table = getTablePart().getTableViewer().getTable();
		int index = table.getSelectionIndex();
		if (index == table.getItemCount() - 1)
			return;
		swap(index, index + 1);
	}

	public void swap(int index1, int index2) {
		// Table table = getTablePart().getTableViewer().getTable();
//		IPluginImport dep1 = ((ImportObject) table.getItem(index1).getData()).getImport();
//		IPluginImport dep2 = ((ImportObject) table.getItem(index2).getData()).getImport();
//
//		try {
//			IPluginModelBase model = (IPluginModelBase) getPage().getModel();
//			IPluginBase pluginBase = model.getPluginBase();
//			pluginBase.swap(dep1, dep2);
//		} catch (CoreException e) {
//			PDEPlugin.logException(e);
//		}
	}

	public void initialize() {
		PluginPackageModel model = (PluginPackageModel)getPage().getModel();
		if (model == null)
			return;
		fViewer.setInput(model);
		updateButtons();
		model.addModelChangedListener(this);
		fAddAction.setEnabled(model.isEditable());
		fRemoveAction.setEnabled(model.isEditable());
	}

	private void makeActions() {
		fAddAction = new Action(LangMessages.PortalJarsSection_add) {
			public void run() {
				handleAdd();
			}
		};
		
		fRemoveAction = new Action(LangMessages.PortalJarsSection_remove) {
			public void run() {
				handleRemove();
			}
		};
		
	}

	public void refresh() {
		fJars = null;
		fViewer.refresh();
		super.refresh();
	}

	public void modelChanged(IModelChangedEvent event) {
		if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
			markStale();
			return;
		}
		
		if (event.getChangedProperty() == IPluginPackageModel.PROPERTY_PORTAL_DEPENDENCY_JARS) {
			refresh();
			updateButtons();
			return;
		}
//		if (event.getChangedProperty() == IPluginBase.P_IMPORT_ORDER) {
//			refresh();
//			updateButtons();
//			return;
//		}
//
//		Object[] changedObjects = event.getChangedObjects();
//		if (changedObjects[0] instanceof IPluginImport) {
//			int index = 0;
//			for (int i = 0; i < changedObjects.length; i++) {
//				Object changeObject = changedObjects[i];
//				IPluginImport iimport = (IPluginImport) changeObject;
//				if (event.getChangeType() == IModelChangedEvent.INSERT) {
//					ImportObject iobj = new ImportObject(iimport);
//					if (fImports == null) {
//						// createImportObjects method will find new addition
//						createImportObjects();
//					} else {
//						int insertIndex = getImportInsertIndex();
//						if (insertIndex < 0) {
//							// Add Button
//							fImports.add(iobj);
//						} else {
//							// DND
//							fImports.add(insertIndex, iobj);
//						}
//					}
//				} else {
//					ImportObject iobj = findImportObject(iimport);
//					if (iobj != null) {
//						if (event.getChangeType() == IModelChangedEvent.REMOVE) {
//							if (fImports == null)
//								// createImportObjects method will not include the removed import
//								createImportObjects();
//							else
//								fImports.remove(iobj);
//							Table table = fImportViewer.getTable();
//							index = table.getSelectionIndex();
//							fImportViewer.remove(iobj);
//						} else {
//							fImportViewer.update(iobj, null);
//						}
//					}
//				}
//			}
//			if (event.getChangeType() == IModelChangedEvent.INSERT) {
//				if (changedObjects.length > 0) {
//					// Refresh the viewer
//					fImportViewer.refresh();
//					// Get the last import added to the viewer
//					IPluginImport lastImport = (IPluginImport) changedObjects[changedObjects.length - 1];
		// // Find the corresponding bundle object for the plugin import
//					ImportObject lastImportObject = findImportObject(lastImport);
//					if (lastImportObject != null) {
//						fImportViewer.setSelection(new StructuredSelection(lastImportObject));
//					}
//					fImportViewer.getTable().setFocus();
//				}
//			} else if (event.getChangeType() == IModelChangedEvent.REMOVE) {
//				Table table = fImportViewer.getTable();
//				table.setSelection(index < table.getItemCount() ? index : table.getItemCount() - 1);
//			}
//		} else {
//			fImportViewer.update(((IStructuredSelection) fImportViewer.getSelection()).toArray(), null);
//		}
	}

//	public void modelsChanged(PluginModelDelta delta) {
//		fImports = null;
//		final Control control = fImportViewer.getControl();
//		if (!control.isDisposed()) {
//			control.getDisplay().asyncExec(new Runnable() {
//				public void run() {
//					if (!control.isDisposed())
//						fImportViewer.refresh();
//				}
//			});
//		}
//	}


	public void setFocus() {
		if (fViewer != null)
			fViewer.getTable().setFocus();
	}

	protected boolean createCount() {
		return true;
	}

	public void propertyChange(PropertyChangeEvent event) {
		// if (fSortAction.equals(event.getSource()) && IAction.RESULT.equals(event.getProperty())) {
		// updateUpDownButtons();
		// }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#isDragAndDropEnabled()
	 */
	protected boolean isDragAndDropEnabled() {
		return false;
	}

	// private boolean isTreeViewerSorted() {
	// if (fSortAction == null) {
	// return false;
	// }
	// return fSortAction.isChecked();
	// }

}
