package org.evosuite.eclipse.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.dialogs.PropertyPage;

public class EvosuitePropertyPage extends PropertyPage {

	private Combo criterionCombo;

	private Button assertionButton;

	private Button minimizeTestsButton;

	private Button minimizeValuesButton;

	private Button reportButton;

	private Button plotButton;

	private Button sandboxButton;

	private Button deterministicButton;

	private Button errorButton;

	private Button contractsButton;

	private Button dseButton;

	// private Button evosuiteRunnerButton;

	private Spinner time;

	private Spinner time2;

	public static QualifiedName CRITERION_PROP_KEY = new QualifiedName("EvoSuite",
	        "Coverage criterion");

	public static QualifiedName ASSERTION_PROP_KEY = new QualifiedName("EvoSuite",
	        "Assertions");

	public static QualifiedName MINIMIZE_TESTS_PROP_KEY = new QualifiedName("EvoSuite",
	        "Minimize tests");

	public static QualifiedName MINIMIZE_VALUES_PROP_KEY = new QualifiedName("EvoSuite",
	        "Minimize values");

	public static QualifiedName TIME_PROP_KEY = new QualifiedName("EvoSuite",
	        "TestGenTime");

	public static QualifiedName REPLACEMENT_TIME_PROP_KEY = new QualifiedName("EvoSuite",
	        "ReplacementGenTime");

	public static QualifiedName PLOT_PROP_KEY = new QualifiedName("EvoSuite", "PlotData");

	public static QualifiedName REPORT_PROP_KEY = new QualifiedName("EvoSuite",
	        "ShowReport");

	public static QualifiedName SANDBOX_PROP_KEY = new QualifiedName("EvoSuite",
	        "Sandbox");

	public static QualifiedName DETERMINISTIC_PROP_KEY = new QualifiedName("EvoSuite",
	        "Transform nondeterministic calls");

	public static QualifiedName ERROR_BRANCHES_PROP_KEY = new QualifiedName("EvoSuite",
	        "Instrument potential error branches");

	public static QualifiedName CONTRACTS_PROP_KEY = new QualifiedName("EvoSuite",
	        "Check generic object contracts");

	public static QualifiedName DSE_PROP_KEY = new QualifiedName("EvoSuite",
	        "Use GA+DSE hybrid search");

	// public static QualifiedName RUNNER_PROP_KEY = new QualifiedName("EvoSuite",
	//        "Use EvoSuite JUnit runner in generated test suites");

	/**
     * 
     */
	public EvosuitePropertyPage() {
		super();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite myComposite = new Composite(parent, SWT.NONE);
		GridLayout mylayout = new GridLayout(2, false);
		mylayout.marginHeight = 10;
		mylayout.marginWidth = 10;
		myComposite.setLayout(mylayout);

		Label criterionlabel = new Label(myComposite, SWT.NONE);
		criterionlabel.setLayoutData(new GridData());
		criterionlabel.setText("Coverage criterion");
		criterionCombo = new Combo(myComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		criterionCombo.add("Branch coverage");
		criterionCombo.add("DefUse coverage");
		criterionCombo.add("Weak mutation testing");
		criterionCombo.add("Strong mutation testing");
		String criterion = getCriterion();
		if (criterion.equals("defuse"))
			criterionCombo.select(1);
		else if (criterion.equals("weakmutation"))
			criterionCombo.select(2);
		else if (criterion.equals("strongmutation"))
			criterionCombo.select(3);
		else
			criterionCombo.select(0);
		criterionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label mylabel = new Label(myComposite, SWT.NONE);
		mylabel.setLayoutData(new GridData());
		mylabel.setText("Create assertions");
		assertionButton = new Button(myComposite, SWT.CHECK);
		assertionButton.setSelection(getAssertionsEnabled());
		assertionButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label mylabelMinTests = new Label(myComposite, SWT.NONE);
		mylabelMinTests.setLayoutData(new GridData());
		mylabelMinTests.setText("Minimize tests");
		minimizeTestsButton = new Button(myComposite, SWT.CHECK);
		minimizeTestsButton.setSelection(getMinimizeTestsEnabled());
		minimizeTestsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label mylabelMinValues = new Label(myComposite, SWT.NONE);
		mylabelMinValues.setLayoutData(new GridData());
		mylabelMinValues.setText("Minimize values");
		minimizeValuesButton = new Button(myComposite, SWT.CHECK);
		minimizeValuesButton.setSelection(getMinimizeValuesEnabled());
		minimizeValuesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label timelabel = new Label(myComposite, SWT.NONE);
		timelabel.setLayoutData(new GridData());
		timelabel.setText("Test generation time (s)");
		time = new Spinner(myComposite, SWT.BORDER);
		time.setMinimum(0);
		time.setMaximum(600);
		//time.setDigits(3);
		time.setSelection(getTime());
		//time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/*
		Label timelabel2 = new Label(myComposite, SWT.NONE);
		timelabel2.setLayoutData(new GridData());
		timelabel2.setText("Test replacement time (s)");
		time2 = new Spinner(myComposite, SWT.BORDER);
		time2.setMinimum(0);
		time2.setMaximum(600);
		//time.setDigits(3);
		time2.setSelection(getReplacementTime());
		 */

		Label mylabel2 = new Label(myComposite, SWT.NONE);
		mylabel2.setLayoutData(new GridData());
		mylabel2.setText("Show report after test generation");
		reportButton = new Button(myComposite, SWT.CHECK);
		reportButton.setSelection(getReportEnabled());
		reportButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reportButton.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				plotButton.setEnabled(((Button) arg0.getSource()).getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				plotButton.setEnabled(((Button) arg0.getSource()).getSelection());
			}
		});

		Label mylabel2a = new Label(myComposite, SWT.NONE);
		mylabel2a.setLayoutData(new GridData());
		mylabel2a.setText("Include plots in report (requires GNUPlot)");
		plotButton = new Button(myComposite, SWT.CHECK);
		plotButton.setSelection(getPlotEnabled());
		plotButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		plotButton.setEnabled(getReportEnabled());

		Label mylabel3 = new Label(myComposite, SWT.NONE);
		mylabel3.setLayoutData(new GridData());
		mylabel3.setText("Use sandbox");
		sandboxButton = new Button(myComposite, SWT.CHECK);
		sandboxButton.setSelection(getSandboxEnabled());
		sandboxButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//		Label mylabel7 = new Label(myComposite, SWT.NONE);
		//		mylabel7.setLayoutData(new GridData());
		//		mylabel7.setText("Use EvoSuite JUnit runner in generated test suites");
		//		evosuiteRunnerButton = new Button(myComposite, SWT.CHECK);
		//		evosuiteRunnerButton.setSelection(getEvoSuiteRunnerEnabled());
		//		evosuiteRunnerButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//		evosuiteRunnerButton.addSelectionListener(new SelectionListener() {
		//			/* (non-Javadoc)
		//			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		//			 */
		//			@Override
		//			public void widgetSelected(SelectionEvent arg0) {
		//				deterministicButton.setEnabled(((Button) arg0.getSource()).getSelection());
		//			}
		//
		//			@Override
		//			public void widgetDefaultSelected(SelectionEvent arg0) {
		//				deterministicButton.setEnabled(((Button) arg0.getSource()).getSelection());
		//			}
		//		});
		//		
		Label mylabel4 = new Label(myComposite, SWT.NONE);
		mylabel4.setLayoutData(new GridData());
		mylabel4.setText("Transform nondeterministic calls\n (e.g. System.currentTimeMillis)");
		deterministicButton = new Button(myComposite, SWT.CHECK);
		deterministicButton.setSelection(getDeterministicEnabled());
		deterministicButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// deterministicButton.setEnabled(getDeterministicEnabled());

		Label mylabel6 = new Label(myComposite, SWT.NONE);
		mylabel6.setLayoutData(new GridData());
		mylabel6.setText("Instrument potential error conditions");
		errorButton = new Button(myComposite, SWT.CHECK);
		errorButton.setSelection(getErrorBranchesEnabled());
		errorButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label mylabel5 = new Label(myComposite, SWT.NONE);
		mylabel5.setLayoutData(new GridData());
		mylabel5.setText("Check generic object contracts");
		contractsButton = new Button(myComposite, SWT.CHECK);
		contractsButton.setSelection(getContractsEnabled());
		contractsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label mylabel7 = new Label(myComposite, SWT.NONE);
		mylabel7.setLayoutData(new GridData());
		mylabel7.setText("Enable DSE during search");
		dseButton = new Button(myComposite, SWT.CHECK);
		dseButton.setSelection(getDSEEnabled());
		dseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return myComposite;

		/*
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		//Label enableLabel = new Label(composite, SWT.NONE);
		//enableLabel.setText("Enable evosuite for this project: ");

		Map<String, Set<Field>> fieldMap = new HashMap<String, Set<Field>>();
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (!fieldMap.containsKey(p.group()))
					fieldMap.put(p.group(), new HashSet<Field>());

				fieldMap.get(p.group()).add(f);
			}
		}

		for (String groupName : fieldMap.keySet()) {
			if (groupName.equals("Runtime"))
				continue;
			Label groupLabel = new Label(parent, SWT.SHADOW_IN);
			groupLabel.setText(groupName);

			//Group groupComposite = new Group(parent, SWT.NONE);
			//groupComposite.setText(groupName);
			//groupComposite.setSize(400, 200);

			Composite groupComposite = new Composite(parent, SWT.NONE);
			GridLayout groupLayout = new GridLayout(2, false);
			groupComposite.setLayout(groupLayout);
			GridData groupData = new GridData(GridData.FILL);
			groupData.grabExcessHorizontalSpace = true;
			//groupData.widthHint = 400;
			groupComposite.setLayoutData(groupData);

			for (Field field : fieldMap.get(groupName)) {
				Parameter p = field.getAnnotation(Parameter.class);
				Label optionLabel = new Label(groupComposite, SWT.WRAP);
				optionLabel.setText(p.description());
				optionLabel.setSize(200, 30);
				try {
					if (field.getType().equals(boolean.class)) {
						Button button = new Button(groupComposite, SWT.CHECK);
						button.setSelection(field.getBoolean(null));
					} else if (field.getType().equals(int.class)) {
						Spinner spinner = new Spinner(groupComposite, SWT.BORDER);
						spinner.setMinimum(0);
						spinner.setMaximum(1000);
						spinner.setSelection(50);
					} else if (field.getType().equals(float.class)) {
						Text text = new Text(groupComposite, SWT.BORDER);
						text.setText("" + field.getFloat(null));
					} else if (field.getType().equals(double.class)) {
						Text text = new Text(groupComposite, SWT.BORDER);
						text.setText("" + field.getDouble(null));
					} else if (field.getType().equals(String.class)) {
						Text text = new Text(groupComposite, SWT.BORDER);
						text.setText((String) field.get(null));
					} else {
						Label label = new Label(groupComposite, SWT.NONE);
						label.setText("Type not yet supported");
					}
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return composite;
		*/
	}

	protected boolean getAssertionsEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(ASSERTION_PROP_KEY);
			if (value == null)
				return true;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return true;
		}
	}

	protected void setAssertionsEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "true";
		try {
			resource.setPersistentProperty(ASSERTION_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getMinimizeTestsEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(MINIMIZE_TESTS_PROP_KEY);
			if (value == null)
				return true;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return true;
		}
	}

	protected void setMinimizeTestsEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "true";
		try {
			resource.setPersistentProperty(MINIMIZE_TESTS_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getMinimizeValuesEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(MINIMIZE_VALUES_PROP_KEY);
			if (value == null)
				return true;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return true;
		}
	}

	protected void setMinimizeValuesEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "false";
		try {
			resource.setPersistentProperty(MINIMIZE_VALUES_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getSandboxEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(SANDBOX_PROP_KEY);
			if (value == null)
				return true;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return false;
		}
	}

	protected void setSandboxEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "true";
		try {
			resource.setPersistentProperty(SANDBOX_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getDeterministicEnabled() {
		// if(!getEvoSuiteRunnerEnabled())
		//	return false;

		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(DETERMINISTIC_PROP_KEY);
			if (value == null)
				return false;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return false;
		}
	}

	protected void setDeterministicEnabled(boolean enabled) {
		//if(!getEvoSuiteRunnerEnabled())
		//	return;

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "false";
		try {
			resource.setPersistentProperty(DETERMINISTIC_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getContractsEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(CONTRACTS_PROP_KEY);
			if (value == null)
				return false;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return false;
		}
	}

	protected void setContractsEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "false";
		try {
			resource.setPersistentProperty(CONTRACTS_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getErrorBranchesEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(ERROR_BRANCHES_PROP_KEY);
			if (value == null)
				return false;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return false;
		}
	}

	protected void setErrorBranchesEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "false";
		try {
			resource.setPersistentProperty(ERROR_BRANCHES_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}
	
	protected boolean getDSEEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(DSE_PROP_KEY);
			if (value == null)
				return false;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return false;
		}
	}

	protected void setDSEEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "false";
		try {
			resource.setPersistentProperty(DSE_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getReportEnabled() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(REPORT_PROP_KEY);
			if (value == null)
				return false;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return false;
		}
	}

	protected void setReportEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "false";
		if (value.equals("true"))
			plotButton.setEnabled(true);
		else
			plotButton.setEnabled(false);
		try {

			resource.setPersistentProperty(REPORT_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected boolean getPlotEnabled() {
		if (!getReportEnabled())
			return false;

		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(PLOT_PROP_KEY);
			if (value == null)
				return false;
			return Boolean.parseBoolean(value);
		} catch (CoreException e) {
			return false;
		}
	}

	protected void setPlotEnabled(boolean enabled) {

		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Boolean.toString(enabled);
		if (value.equals(""))
			value = "false";
		//if (value.equals("true"))
		//	deterministicButton.setEnabled(true);
		//else
		//	deterministicButton.setEnabled(false);

		try {
			resource.setPersistentProperty(PLOT_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	//	protected boolean getEvoSuiteRunnerEnabled() {
	//		IResource resource = ((IJavaProject) getElement()).getResource();
	//		try {
	//			String value = resource.getPersistentProperty(RUNNER_PROP_KEY);
	//			if (value == null)
	//				return false;
	//			return Boolean.parseBoolean(value);
	//		} catch (CoreException e) {
	//			return false;
	//		}
	//	}
	//
	//	protected void setEvoSuiteRunnerEnabled(boolean enabled) {
	//
	//		IResource resource = ((IJavaProject) getElement()).getResource();
	//		String value = Boolean.toString(enabled);
	//		if (value.equals(""))
	//			value = "false";
	//		try {
	//			resource.setPersistentProperty(RUNNER_PROP_KEY, value);
	//		} catch (CoreException e) {
	//		}
	//	}

	protected int getTime() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(TIME_PROP_KEY);
			if (value == null)
				return 60;
			return Integer.parseInt(value);
		} catch (CoreException e) {
			return 60;
		}
	}

	protected void setTime(int time) {
		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Integer.toString(time);
		if (value.equals(""))
			value = "60";
		try {
			resource.setPersistentProperty(TIME_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	protected String getCriterion() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(CRITERION_PROP_KEY);
			if (value == null)
				return "branch";
			else
				return value;
		} catch (CoreException e) {
			return "branch";
		}
	}

	protected void setCriterion(String value) {
		IResource resource = ((IJavaProject) getElement()).getResource();
		if (value.equals(""))
			value = "branch";
		String criterion = value.toLowerCase();
		try {
			if (criterion.startsWith("defuse"))
				resource.setPersistentProperty(CRITERION_PROP_KEY, "defuse");
			else if (criterion.startsWith("weak"))
				resource.setPersistentProperty(CRITERION_PROP_KEY, "weakmutation");
			else if (criterion.startsWith("strong"))
				resource.setPersistentProperty(CRITERION_PROP_KEY, "strongmutation");
			else
				resource.setPersistentProperty(CRITERION_PROP_KEY, "branch");
		} catch (CoreException e) {
		}
	}

	protected int getReplacementTime() {
		IResource resource = ((IJavaProject) getElement()).getResource();
		try {
			String value = resource.getPersistentProperty(REPLACEMENT_TIME_PROP_KEY);
			if (value == null)
				return 10;
			return Integer.parseInt(value);
		} catch (CoreException e) {
			return 10;
		}
	}

	protected void setReplacementTime(int time) {
		IResource resource = ((IJavaProject) getElement()).getResource();
		String value = Integer.toString(time);
		if (value.equals(""))
			value = "10";
		try {
			resource.setPersistentProperty(REPLACEMENT_TIME_PROP_KEY, value);
		} catch (CoreException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		setReportEnabled(reportButton.getSelection());
		setPlotEnabled(plotButton.getSelection());
		setAssertionsEnabled(assertionButton.getSelection());
		setMinimizeTestsEnabled(minimizeTestsButton.getSelection());
		setMinimizeValuesEnabled(minimizeValuesButton.getSelection());
		setSandboxEnabled(sandboxButton.getSelection());
		setDeterministicEnabled(deterministicButton.getSelection());
		setTime(time.getSelection());
		setCriterion(criterionCombo.getText());
		setContractsEnabled(contractsButton.getSelection());
		setErrorBranchesEnabled(errorButton.getSelection());
		setDSEEnabled(dseButton.getSelection());
		// setEvoSuiteRunnerEnabled(evosuiteRunnerButton.getSelection());
		//setReplacementTime(time2.getSelection());
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		setTime(60);
		//setReplacementTime(10);
		setAssertionsEnabled(true);
		setMinimizeTestsEnabled(true);
		setMinimizeValuesEnabled(false);
		setReportEnabled(false);
		setPlotEnabled(false);
		setSandboxEnabled(true);
		setDeterministicEnabled(false);
		setContractsEnabled(false);
		setErrorBranchesEnabled(false);
		setDSEEnabled(false);
		// setEvoSuiteRunnerEnabled(false);
		setCriterion("branch");
	}
}