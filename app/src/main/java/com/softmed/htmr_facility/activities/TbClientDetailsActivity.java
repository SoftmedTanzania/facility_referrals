package com.softmed.htmr_facility.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import com.irozon.alertview.AlertActionStyle;
import com.irozon.alertview.AlertStyle;
import com.irozon.alertview.AlertView;
import com.irozon.alertview.objects.AlertAction;
import com.softmed.htmr_facility.R;
import com.softmed.htmr_facility.adapters.AppointmentSpinnerAdapter;
import com.softmed.htmr_facility.base.AppDatabase;
import com.softmed.htmr_facility.base.BaseActivity;
import com.softmed.htmr_facility.dom.objects.Patient;
import com.softmed.htmr_facility.dom.objects.PatientAppointment;
import com.softmed.htmr_facility.dom.objects.PostOffice;
import com.softmed.htmr_facility.dom.objects.Referral;
import com.softmed.htmr_facility.dom.objects.TbEncounters;
import com.softmed.htmr_facility.dom.objects.TbPatient;
import com.softmed.htmr_facility.utils.EncountersDiffCallback;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch;
import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import fr.ganfra.materialspinner.MaterialSpinner;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.softmed.htmr_facility.utils.constants.ENTRY_NOT_SYNCED;
import static com.softmed.htmr_facility.utils.constants.FEMALE;
import static com.softmed.htmr_facility.utils.constants.FEMALE_SW;
import static com.softmed.htmr_facility.utils.constants.FEMALE_VALUE;
import static com.softmed.htmr_facility.utils.constants.MALE;
import static com.softmed.htmr_facility.utils.constants.MALE_SW;
import static com.softmed.htmr_facility.utils.constants.MALE_VALUE;
import static com.softmed.htmr_facility.utils.constants.MATOKEO_AMEFARIKI;
import static com.softmed.htmr_facility.utils.constants.MATOKEO_AMEHAMA;
import static com.softmed.htmr_facility.utils.constants.MATOKEO_AMEMALIZA_TIBA;
import static com.softmed.htmr_facility.utils.constants.MATOKEO_AMEPONA;
import static com.softmed.htmr_facility.utils.constants.MATOKEO_HAKUPONA;
import static com.softmed.htmr_facility.utils.constants.POST_DATA_REFERRAL_FEEDBACK;
import static com.softmed.htmr_facility.utils.constants.POST_DATA_TYPE_APPOINTMENTS;
import static com.softmed.htmr_facility.utils.constants.POST_DATA_TYPE_ENCOUNTER;
import static com.softmed.htmr_facility.utils.constants.POST_DATA_TYPE_PATIENT;
import static com.softmed.htmr_facility.utils.constants.POST_DATA_TYPE_TB_PATIENT;
import static com.softmed.htmr_facility.utils.constants.REFERRAL_STATUS_COMPLETED;
import static com.softmed.htmr_facility.utils.constants.STATUS_COMPLETED_VAL;
import static com.softmed.htmr_facility.utils.constants.STATUS_PENDING_VAL;
import static com.softmed.htmr_facility.utils.constants.TB_1_PLUS;
import static com.softmed.htmr_facility.utils.constants.TB_2_PLUS;
import static com.softmed.htmr_facility.utils.constants.TB_3_PLUS;
import static com.softmed.htmr_facility.utils.constants.TB_NEGATIVE;
import static com.softmed.htmr_facility.utils.constants.TB_SCANTY;
import static com.softmed.htmr_facility.utils.constants.TREATMENT_TYPE_1;
import static com.softmed.htmr_facility.utils.constants.TREATMENT_TYPE_2;
import static com.softmed.htmr_facility.utils.constants.TREATMENT_TYPE_3;
import static com.softmed.htmr_facility.utils.constants.TREATMENT_TYPE_4;
import static com.softmed.htmr_facility.utils.constants.TREATMENT_TYPE_5;
import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

/**
 * Created by issy on 12/28/17.
 *
 * @issyzac issyzac.iz@gmail.com
 * On Project HFReferralApp
 */

public class TbClientDetailsActivity extends BaseActivity {

    private static final String TAG = "TbClientDetailsActivity";
    public static final String CURRENT_PATIENT = "patient";
    public static final String ORIGIN_STATUS = "originStatus";
    public static final String PATIENT_NEW = "patient_new";

    public static final int FROM_REFERRALS = 1;
    public static final int FROM_CLIENTS = 2;
    public static final int RESTARTING_TREATMENT = 3;

    LinearLayout matokeoLinearLayout, restartTreatmentButton;
    RelativeLayout finishedPreviousMonthLayout, makohoziWrapper, othersWrapper, makohoziEncounterWrap;
    private MaterialSpinner matibabuSpinner, matokeoSpinner, makohoziSpinner, encouterMonthSpinner, monthOneMakohoziSpinner, appointmentsSpinner;
    TextView patientNames, patientGender, patientAge, patientWeight, phoneNumber;
    TextView ward, village, hamlet, medicationStatusTitle, resultsDate, emptyPreviousMonthEncounter;
    TextView currentEncounterNumber, encounterDate, encounterAppointmentDate, finishedTreatmentTitle;
    EditText outcomeDetails, otherTestValue, monthlyPatientWeightEt;
    Button saveButton, cancelButton;
    ProgressDialog dialog;
    CheckBox medicationStatusCheckbox, finishedTreatmentToggle;
    ToggleSwitch testTypeToggle;
    View encounterUI, testUI, treatmentUI, resultsUI, demographicUI, clickBlocker;
    RecyclerView previousEncountersRecycler;
    TableLayout previousEncoutnersTitleTable;
    TableRow previousEncountersTitleTableRow;

    Patient currentPatient;
    TbPatient currentTbPatient;
    TbEncounters currentPatientEncounter;
    Referral currentReferral;
    PatientAppointment selectedEncounterAppointment;

    boolean patientNew;
    boolean activityCanExit = false;
    int selectedTestType = 0; //0 -> default to 0 selected test type
    int encounterNumber = 1; //1 -> default to the first encounter
    final String[] tbTypesFirstEncounter = {TB_SCANTY, TB_1_PLUS, TB_2_PLUS, TB_3_PLUS};
    final String[] tbTypes = {TB_NEGATIVE, TB_SCANTY, TB_1_PLUS, TB_2_PLUS, TB_3_PLUS};
    String strMatibabu, strXray, strVipimoVingine, strMakohozi, strMonth, strOutcome, strOutcomeDetails, strOutcomeDate;
    String otherTestValueString = "";
    String[] treatmentTypes = {TREATMENT_TYPE_1, TREATMENT_TYPE_2, TREATMENT_TYPE_3, TREATMENT_TYPE_4, TREATMENT_TYPE_5};
    ArrayAdapter<String> makohoziSpinnerAdapter, monthOneMakohoziAdapter;
    Context context;
    Calendar resultCalendar;
    long outcomeDate = 0;
    String currentPatientId;
    int originStatus = 0;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tb_client_details);
        setupviews();

        context = this;

        dialog = new ProgressDialog(TbClientDetailsActivity.this, 0);
        dialog.setTitle(getResources().getString(R.string.saving));
        dialog.setMessage(getResources().getString(R.string.loading_please_wait));

        if (getIntent().getExtras() != null){
            originStatus = getIntent().getIntExtra(ORIGIN_STATUS, 0);
            switch (originStatus){
                case 1:
                    //From Referral List
                    patientNew = true;
                    calibrateUI(true);
                    currentReferral = (Referral) getIntent().getSerializableExtra("referral");
                    new AsyncTask<String, Void, Void>(){

                        Patient _patient;

                        @Override
                        protected Void doInBackground(String... strings) {
                            _patient = baseDatabase.patientModel().getPatientById(strings[0]);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            displayPatientInformation(_patient);
                        }
                    }.execute(currentReferral.getPatient_id());
                    break;
                case 2:
                    //From Tb Clients List
                    patientNew = false;
                    calibrateUI(false);
                    currentPatient = (Patient) getIntent().getSerializableExtra(CURRENT_PATIENT);
                    displayPatientInformation(currentPatient);
                    break;
                case 3:
                    //From restarting regime
                    patientNew = true;
                    calibrateUI(true);
                    currentPatient = (Patient) getIntent().getSerializableExtra(CURRENT_PATIENT);
                    displayPatientInformation(currentPatient);
                    break;
            }
        }

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item_black, treatmentTypes);
        spinAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_black);
        matibabuSpinner.setAdapter(spinAdapter);

        matibabuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("BILLION", "position "+i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final String[] matokeo = {MATOKEO_AMEPONA, MATOKEO_AMEMALIZA_TIBA, MATOKEO_AMEFARIKI, MATOKEO_AMEHAMA, MATOKEO_HAKUPONA};
        ArrayAdapter<String> matokepSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item_black, matokeo);
        matokepSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_black);
        matokeoSpinner.setAdapter(matokepSpinnerAdapter);

        //START HERE ..::.. This will not be needed (Manual encounter selector implementation)
        final String[] encounterMonths = {"1", "2", "3", "4", "5", "6", "7", "8"};
        ArrayAdapter<String> encounterMonthSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item_black, encounterMonths);
        encounterMonthSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_black);
        encouterMonthSpinner.setAdapter(encounterMonthSpinnerAdapter);

        makohoziSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item_black, tbTypesFirstEncounter);
        makohoziSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_black);
        makohoziSpinner.setAdapter(makohoziSpinnerAdapter);

        monthOneMakohoziAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item_black, tbTypes);
        monthOneMakohoziAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_black);
        monthOneMakohoziSpinner.setAdapter(makohoziSpinnerAdapter);

        if (patientNew){
            selectedTestType = 1;
        }

        //Handling what happens when user selects the test types that have been conducted to a client
        testTypeToggle.setOnToggleSwitchChangeListener(new BaseToggleSwitch.OnToggleSwitchChangeListener() {
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                if (position == 0 && isChecked){
                    //Sputum for AFB selected
                    makohoziWrapper.setVisibility(View.VISIBLE);
                    othersWrapper.setVisibility(View.GONE);
                    selectedTestType = 1;
                    Log.d("aways", "Test Type : "+selectedTestType);
                }else if (position == 1 && isChecked){
                    //X-Ray selected
                    makohoziWrapper.setVisibility(View.GONE);
                    othersWrapper.setVisibility(View.GONE);
                    selectedTestType = 2;
                    Log.d("aways", "Test Type : "+selectedTestType);
                }else {
                    //Other tests selected
                    makohoziWrapper.setVisibility(View.GONE);
                    othersWrapper.setVisibility(View.VISIBLE);
                    selectedTestType = 3;
                    Log.d("aways", "Test Type : "+selectedTestType);
                }
            }
        });

        //Check if the patient have finished treatment
        finishedTreatmentToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    resultsUI.setVisibility(View.VISIBLE);
                }else {
                    resultsUI.setVisibility(View.GONE);
                }
            }
        });

        //Select the date of the results of medication
        resultsDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show(getFragmentManager(),"dateOfBirth");
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

                        resultsDate.setText((dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "-" + ((monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : monthOfYear + 1) + "-" + year);
                        resultCalendar = Calendar.getInstance();
                        resultCalendar.set(year, monthOfYear, dayOfMonth);
                        outcomeDate = resultCalendar.getTimeInMillis();
                    }

                });
            }
        });

        //Handling storing of data
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (patientNew){
                    //Save Test Information
                    //Save Treatment
                    activityCanExit = true;

                    if (currentReferral != null){
                        //Ending the current Referral
                        new AsyncTask<Void, Void, Void>(){

                            @Override
                            protected Void doInBackground(Void... voids) {
                                //End Current Referral
                                currentReferral.setReferralStatus(REFERRAL_STATUS_COMPLETED);
                                currentReferral.setServiceGivenToPatient(context.getResources().getString(R.string.received_at_tb_clinic));
                                currentReferral.setOtherNotesAndAdvices("");
                                currentReferral.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
                                baseDatabase.referalModel().updateReferral(currentReferral);

                                //Add Post office entry
                                PostOffice postOffice = new PostOffice();
                                postOffice.setPost_id(currentReferral.getReferral_id());
                                postOffice.setPost_data_type(POST_DATA_REFERRAL_FEEDBACK);
                                postOffice.setSyncStatus(ENTRY_NOT_SYNCED);
                                baseDatabase.postOfficeModelDao().addPostEntry(postOffice);

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                new EnrollPatientToTbClinic(baseDatabase).execute(currentReferral.getPatient_id());
                            }
                        }.execute();
                    }else {
                        //Restarting the treatment regime for existing patient
                        new EnrollPatientToTbClinic(baseDatabase).execute(currentPatient.getPatientId());
                    }


                }else {
                    //Save Encounter
                    /*If month >= 6
                     Save Results
                    */
                    if (saveEncounters(currentTbPatient.getTestType(), encounterNumber)){

                        //Update appointment corresponding to this encounter
                        updateAppointment(currentPatientEncounter);

                        if (finishedTreatmentToggle.isChecked()){
                            //Save Treatment Results;
                            activityCanExit = false;
                            if (saveResults()){
                                activityCanExit = true;
                                new AsyncTask<Void, Void, Void>(){
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        currentPatient.setCurrentOnTbTreatment(false);

                                        baseDatabase.patientModel().updatePatient(currentPatient);

                                        PostOffice postOffice = new PostOffice();
                                        postOffice.setSyncStatus(ENTRY_NOT_SYNCED);
                                        postOffice.setPost_id(currentPatient.getPatientId());
                                        postOffice.setPost_data_type(POST_DATA_TYPE_PATIENT);

                                        baseDatabase.postOfficeModelDao().addPostEntry(postOffice);

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);

                                        new SaveTbPatientTask(baseDatabase).execute(currentTbPatient);

                                    }
                                }.execute();
                            }
                        }else {
                            activityCanExit = true;
                        }

                        new SaveEncounters(baseDatabase).execute(currentPatientEncounter);
                    }
                }
            }
        });

        //Cancel button has been clicked
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        restartTreatmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = TbClientDetailsActivity.this;
                AlertView alert = new AlertView("Restart "+ patientNames.getText().toString()+ "'s Treatment", "Are you sure you want to restart this patient's treatment?", AlertStyle.DIALOG);
                alert.addAction(new AlertAction(context.getResources().getString(R.string.answer_no), AlertActionStyle.DEFAULT, action -> {
                    // Action 1 callback
                }));
                alert.addAction(new AlertAction(context.getResources().getString(R.string.answer_yes), AlertActionStyle.NEGATIVE, action -> {

                    /*
                    TREATMENT STATUS
                    1 -> On going treatment
                    0 -> Cancelled treatment
                     */
                    currentTbPatient.setTreatmentStatus(0);

                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {

                            baseDatabase.tbPatientModelDao().updateTbPatient(currentTbPatient);

                            PostOffice postOffice = new PostOffice();
                            postOffice.setPost_data_type(POST_DATA_TYPE_TB_PATIENT);
                            postOffice.setPost_id(String.valueOf(currentTbPatient.getTbPatientId()));
                            postOffice.setSyncStatus(ENTRY_NOT_SYNCED);

                            //Storing data to post office ready to be saved to the server
                            baseDatabase.postOfficeModelDao().addPostEntry(postOffice);

                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            //2: Restart this activity to create a new Tb Client
                            Intent intent = new Intent(TbClientDetailsActivity.this, TbClientDetailsActivity.class);
                            intent.putExtra(ORIGIN_STATUS, RESTARTING_TREATMENT);
                            intent.putExtra(CURRENT_PATIENT, currentPatient);
                            startActivity(intent);
                            TbClientDetailsActivity.this.finish();
                        }
                    }.execute();

                }));
                alert.show(TbClientDetailsActivity.this);

            }
        });



    }

    @SuppressLint("StaticFieldLeak")
    /**
     * Queries for all unattended appointments and creates a list adapter for the variable @appointmentSpinner
     */
    void getUnattendedAppointments(){
        new AsyncTask<Void, Void, Void>(){
            List<PatientAppointment> appointments = new ArrayList<>();
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG, "Querying list of appointments");
                appointments = baseDatabase.appointmentModelDao().getAppointmentsByTypeAndPatientID(2, currentPatient.getPatientId());
                Log.d(TAG, "Size of obtained appointments "+appointments.size());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                AppointmentSpinnerAdapter adapter = new AppointmentSpinnerAdapter(TbClientDetailsActivity.this,R.layout.simple_spinner_item_black, appointments);
                appointmentsSpinner.setAdapter(adapter);
            }
        }.execute();
    }

    /**
     * Method to display patient's demographic information
     * @param _patient -> Current patient object
     */
    void displayPatientInformation(Patient _patient){

        String names = _patient.getPatientFirstName()+
                " "+ _patient.getPatientMiddleName()+
                " "+ _patient.getPatientSurname();

        patientNames.setText(names);

        if (BaseActivity.getLocaleString().endsWith(ENGLISH_LOCALE)){
            if (_patient.getGender().equals(MALE) || _patient.getGender().equals(MALE_VALUE)){
                patientGender.setText(MALE);
            }else if (_patient.getGender().equals(FEMALE) || _patient.getGender().equals(FEMALE_VALUE)){
                patientGender.setText(FEMALE);
            }
        }else {
            if (_patient.getGender().equals(MALE) || _patient.getGender().equals(MALE_VALUE)){
                patientGender.setText(MALE_SW);
            }else if (_patient.getGender().equals(FEMALE) || _patient.getGender().equals(FEMALE_VALUE)){
                patientGender.setText(FEMALE_SW);
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(_patient.getDateOfBirth());

        patientAge.setText(getDiffYears(calendar.getTime(), new Date())+"");
        phoneNumber.setText(_patient.getPhone_number()==""? "" : _patient.getPhone_number());
        ward.setText(_patient.getWard()==""? "" : _patient.getWard());
        village.setText(_patient.getVillage() == "" ? "" : _patient.getVillage());
        hamlet.setText(_patient.getHamlet() == "" ? "" : _patient.getHamlet());
        patientWeight.setText(""); //save patient weight in patient object so as to be able to display it here

        if (!patientNew){
            new GetTbPatientByPatientID(baseDatabase).execute(_patient.getPatientId());
        }

    }

    /**
     * Sets up the activity by initializing all the view elements associated with this activity
     */
    void setupviews(){

        previousEncoutnersTitleTable = findViewById(R.id.previous_encounters_title_table);
        previousEncountersTitleTableRow = findViewById(R.id.previous_encounters_title_table_row);

        currentEncounterNumber = findViewById(R.id.encounter_number);
        encounterDate = findViewById(R.id.encounter_date);
        encounterAppointmentDate = findViewById(R.id.encounter_appointment_date);

        encounterUI = (View) findViewById(R.id.enconter_ui);
        treatmentUI = (View) findViewById(R.id.treatment_ui);
        testUI = (View) findViewById(R.id.test_ui);
        resultsUI = (View) findViewById(R.id.results_ui);
        resultsUI.setVisibility(View.GONE);
        demographicUI = (View) findViewById(R.id.demographic_ui);
        clickBlocker = (View) findViewById(R.id.view_click_blocker);
        clickBlocker.setVisibility(View.GONE);
        clickBlocker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        testTypeToggle =  findViewById(R.id.test_type);

        //LinearLayout
        matokeoLinearLayout =  findViewById(R.id.matokep_ll);
        restartTreatmentButton = findViewById(R.id.restart_treatment);

        //Relative Layouts
        makohoziWrapper =  findViewById(R.id.makohozi_wrapper);
        othersWrapper = findViewById(R.id.others_wrapper);
        makohoziEncounterWrap =  findViewById(R.id.makohozi_encounter_wrap);
        finishedPreviousMonthLayout =  findViewById(R.id.finished_previous_month_layout);

        //EditTexts
        otherTestValue =  findViewById(R.id.other_test_value);
        outcomeDetails =  findViewById(R.id.other_information);
        monthlyPatientWeightEt =  findViewById(R.id.monthly_uzito_value);

        //Buttons
        saveButton = findViewById(R.id.hifadhi_taarifa);
        cancelButton = findViewById(R.id.cancel_button);

        //TextViews
        patientNames =  findViewById(R.id.names_text);
        patientGender =  findViewById(R.id.gender_text);
        patientAge =  findViewById(R.id.age_text);
        patientWeight =  findViewById(R.id.weight_text);
        phoneNumber =  findViewById(R.id.phone_text);
        ward =  findViewById(R.id.ward_text);
        village =  findViewById(R.id.village_text);
        hamlet =  findViewById(R.id.hamlet_text);
        medicationStatusTitle =  findViewById(R.id.medication_status_title);
        resultsDate =  findViewById(R.id.date);
        emptyPreviousMonthEncounter =  findViewById(R.id.empty_previous_encounters);
        finishedTreatmentTitle = findViewById(R.id.finished_treatment_title);

        //Spinners
        matibabuSpinner =  findViewById(R.id.spin_matibabu);
        matokeoSpinner =  findViewById(R.id.spin_matokeo);
        encouterMonthSpinner =  findViewById(R.id.spin_encounter_month);
        makohoziSpinner =  findViewById(R.id.spin_makohozi);
        monthOneMakohoziSpinner = findViewById(R.id.spin_makohozi_month_one);
        appointmentsSpinner = findViewById(R.id.spin_appointments);

        //Checkboxes
        medicationStatusCheckbox = findViewById(R.id.medication_status);
        finishedTreatmentToggle = findViewById(R.id.finished_treatment_toggle);

        previousEncountersRecycler =  findViewById(R.id.previous_encounters_recycler_view);
        previousEncountersRecycler.setLayoutManager(new LinearLayoutManager(this));
        previousEncountersRecycler.hasFixedSize();
    }

    /**
     * Calibrates the UI based on if its the first time the clients visits the clinic or is returning
     * @param b
     */
    void calibrateUI(boolean b){
        if (b){
            encounterUI.setVisibility(View.GONE);
            resultsUI.setVisibility(View.GONE);
            restartTreatmentButton.setVisibility(View.GONE);
            finishedTreatmentTitle.setVisibility(View.GONE);
            finishedTreatmentToggle.setVisibility(View.GONE);

        }else {
            encounterUI.setVisibility(View.VISIBLE);
            restartTreatmentButton.setVisibility(View.VISIBLE);
            finishedTreatmentTitle.setVisibility(View.VISIBLE);
            finishedTreatmentToggle.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to store test type data (Tests that have been conducted to the client)
     * @return true if has successfully stored the data in the @currentPatient object
     */
    boolean saveTestData(){
        //Patient Test Variables
        String makohoziWeight = "";
        String otherTestDescription = "";

        TbPatient patient = currentTbPatient;

        Log.d("saveTestData", "Saving test data");

        //Validating input test data
        if (selectedTestType==1){
            if (monthOneMakohoziSpinner.getSelectedItemPosition() == 0){
                Toast.makeText(
                        context,
                        "Tafadhali jaza uzito wa makohozi kabla ya matibabu",
                        Toast.LENGTH_LONG
                ).show();
                Log.d("saveTestData", "Test type is makohozi and weight is not selected");
                return false;
            }else {
                makohoziWeight = (String) monthOneMakohoziSpinner.getSelectedItem();
            }
        }else if (selectedTestType==3){
            if (otherTestValue.getText().toString().isEmpty()){
                Toast.makeText(
                        context,
                        "Tafadhali eleza vipimo vilivyofanyika",
                        Toast.LENGTH_LONG
                ).show();
                Log.d("saveTestData", "Test type is other and there is no description");
                return false;
            }else {
                otherTestDescription = otherTestValue.getText().toString();
            }
        }else if (selectedTestType == 2){
            otherTestDescription = "";
            makohoziWeight = "";
        }
        patient.setTestType(selectedTestType);
        patient.setOtherTestDetails(otherTestDescription);
        patient.setMakohozi(makohoziWeight);
        currentTbPatient = patient;

        Log.d("saveTestData", "Done updating Patient object with test info");

        return true;
    }

    /**
     *  Method to store the treatment data that has been selected for the client
     * @return true if has successfully stored the data in the @currentPatient object
     */
    boolean saveTreatmentData (){
        //Patient Treatment Variables
        String selectedMatibabu = "";
        TbPatient patient = currentTbPatient;
        if (matibabuSpinner.getSelectedItemPosition() == 0){
            toastThis(getResources().getString(R.string.fill_treatment_type));
            return false;
        }else {
            selectedMatibabu = (String) matibabuSpinner.getSelectedItem();
        }
        patient.setTreatment_type(selectedMatibabu);
        currentTbPatient = patient;
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    /**
     * Method to store the encounter data for the client everytime they visit the clinic
     * @param testType -> The type of medical tests used upon enrolling the client to the clinic
     * @param encounterNumber -> The number of the visit that the client came for in the clinic
     */
    boolean saveEncounters(int testType, int encounterNumber){

        //TbEncounter Variables
        String makohoziValue = "";
        String monthlyWeight = "";
        int encYear;

        //Create a new @TbEncounters object
        TbEncounters tbEncounter = new TbEncounters();

        //Check if its the 1st or 3rd encounter and require sputum measured if the test type conducted was Sputum for AFB
        if (testType == 1 && (encounterNumber == 1 || encounterNumber == 3 || encounterNumber == 6) ){
            if (makohoziSpinner.getSelectedItemPosition() == 0){
                toastThis(getResources().getString(R.string.fill_sputum_status));
                return false;
            }else {
                makohoziValue = (String) makohoziSpinner.getSelectedItem();
            }
        }

        //Require monthly client general body weight upon every visit
        if (monthlyPatientWeightEt.getText().toString().isEmpty()){
            toastThis(getResources().getString(R.string.fill_client_weight));
            return false;
        }else {
            monthlyWeight = monthlyPatientWeightEt.getText().toString();
        }

        tbEncounter.setTbPatientID(currentTbPatient.getTbPatientId());
        tbEncounter.setEncounterNumber(encounterNumber);

        int encounterYear = Calendar.getInstance().get(Calendar.YEAR);
        tbEncounter.setEncounterYear(encounterYear);

        //Local ID is the local(Device specific) encounter ID generated using patientID, EncounterNumber and EncounterYear
        String localID = tbEncounter.getTbPatientID()+"_"+encounterNumber+"_"+encounterYear;
        tbEncounter.setLocalID(localID);

        //Set the value of sputum weight
        tbEncounter.setMakohozi(makohoziValue);

        //Set if the client had finished previous encounter medication
        tbEncounter.setHasFinishedPreviousMonthMedication(medicationStatusCheckbox.isChecked());

        //Set the general body weight of the client
        tbEncounter.setWeight(monthlyWeight);

        //This is the medication Status of this encounter to be set on the next visit
        tbEncounter.setMedicationDate(Calendar.getInstance().getTimeInMillis());
        tbEncounter.setMedicationStatus(false);

        //Check if the selected encounter appointment is not null
        //The @selectedEncounterAppointment value is set when getting the @currentTbPatient object
        if(selectedEncounterAppointment != null){

            Log.d(TAG, "saveEncounters: Selected Encounter has value");

            //Set the appointment ID of the encounter based on the selected appointment
            tbEncounter.setAppointmentId(selectedEncounterAppointment.getAppointmentID());
            //Scheduled date is the date of the appointment and medication date is the date of the visit to the clinic
            tbEncounter.setScheduledDate(selectedEncounterAppointment.getAppointmentDate());
        }else {
            Log.d(TAG, "saveEncounters: Selected encounter nulll");
        }

        tbEncounter.setMedicationDate(Calendar.getInstance().getTimeInMillis());

        Calendar calendar = Calendar.getInstance();
        long today = calendar.getTimeInMillis();

        tbEncounter.setId(currentTbPatient.getTbPatientId()+"_"+encounterNumber);
        currentPatientEncounter = tbEncounter;
        return true;
    }

    /**
     * Method to store the results of the medication after client have completed for at least 6 visits
     * @return true -> if the data have been captured and stored successfully in the @currentTbPatient object
     */
    boolean saveResults(){

        String outcomeDetailsStr = "";
        String resultsStr = "";
        String dateOfResultsStr = "";

        if (outcomeDetails.getText().toString().isEmpty()){
            toastThis(getResources().getString(R.string.specify_results));
            return false;
        }else {
            outcomeDetailsStr = outcomeDetails.getText().toString();
        }

        if (matokeoSpinner.getSelectedItemPosition() == 0){
            toastThis(getResources().getString(R.string.select_results));
            return false;
        }else {
            resultsStr = (String) matokeoSpinner.getSelectedItem();
        }

        if (resultsDate.getText().toString().isEmpty()){
            toastThis(getResources().getString(R.string.select_treatment_date));
            return false;
        }else {
            dateOfResultsStr = resultsDate.getText().toString();
        }

        currentTbPatient.setOutcome(resultsStr);
        currentTbPatient.setOutcomeDate(outcomeDate);
        currentTbPatient.setTreatmentStatus(2);
        currentTbPatient.setOutcomeDetails(outcomeDetailsStr);

        return true;
    }

    @SuppressLint("StaticFieldLeak")
    void updateAppointment(TbEncounters encounter){

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {

                if (selectedEncounterAppointment != null){
                    selectedEncounterAppointment.setStatus(STATUS_COMPLETED_VAL);
                    baseDatabase.appointmentModelDao().updateAppointment(selectedEncounterAppointment);
                    Log.d(TAG, "Appointment Updated in the database");

                    //add the updated appointment to postoffice
                    PostOffice office = new PostOffice();
                    office.setSyncStatus(ENTRY_NOT_SYNCED);
                    office.setPost_id(selectedEncounterAppointment.getAppointmentID()+"");
                    office.setPost_data_type(POST_DATA_TYPE_APPOINTMENTS);

                    //baseDatabase.postOfficeModelDao().addPostEntry(office); TODO: After implementing Endpoints to send single appointments updates uncomment this

                    Log.d(TAG, "Appointment data added to post office ready to be synced to the server");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();


        /**
         * Old fashion way of updating an appointment status after a client has visited the clinic
         */
        /*
        new AsyncTask<TbEncounters, Void, Void>(){
            @Override
            protected Void doInBackground(TbEncounters... tbEncounters) {

                List<PatientAppointment> thisPatientAppointments = new ArrayList<>();
                thisPatientAppointments = baseDatabase.appointmentModelDao().getAppointmentsByTypeAndPatientID(2, currentPatient.getPatientId());
                for (PatientAppointment a : thisPatientAppointments){
                    long appointmentDate = a.getAppointmentDate();
                    long meddicationDate = tbEncounters[0].getMedicationDate();

                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(appointmentDate);
                    int month = c.get(Calendar.MONTH);

                    c.setTimeInMillis(meddicationDate);
                    int medicationMonth = c.get(Calendar.MONTH);

                    if (month == medicationMonth){
                        Log.d(TAG, "Found the appointment for this patient with month "+month+1);
                        //If found, update the appointment status to attended
                        a.setStatus(STATUS_COMPLETED);

                        baseDatabase.appointmentModelDao().updateAppointment(a);

                        PostOffice appData = new PostOffice();
                        appData.setPost_data_type(POST_DATA_TYPE_APPOINTMENTS);
                        appData.setPost_id(a.getAppointmentID()+"");
                        appData.setSyncStatus(ENTRY_NOT_SYNCED);

                        baseDatabase.postOfficeModelDao().addPostEntry(appData);

                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute(encounter);*/

    }

    /**
     * This method receives the instances of TbPatient of the current healthFacilityPatient and retreaves all the
     * encounter records present in the database for this particulat patient
     *
     * @param tbPatients
     */
    void getEncounterRecords(List<TbPatient> tbPatients){

        List<Dose> doseList = new ArrayList<>();
        PreviousEncountersRecyclerAdapter adapter = new PreviousEncountersRecyclerAdapter(TbClientDetailsActivity.this);
        previousEncountersRecycler.setAdapter(adapter);

        /**
         * An Observable to retreave the encounter information from the background thread
         */
        Observable<Dose> observable = Observable.create(emitter -> {
            try {
                for (TbPatient patient : tbPatients){

                    List<TbEncounters> encounters = baseDatabase.tbEncounterModelDao().getEncounterByPatientID(patient.getTbPatientId());
                    Dose dose = new Dose();
                    int encounterNumber = 1;

                    Log.d("human", "Dose, encounter size : "+encounters.size());

                    if (patient.getTreatmentStatus() == 1){
                        //This is the current ongoing treatment
                        dose.setCurrentDose(true);

                        if (encounters.size() > 0){
                            for (TbEncounters encounter : encounters){
                                if (encounter.getEncounterNumber() > encounterNumber){
                                    encounterNumber = encounter.getEncounterNumber();
                                }
                            }

                            //There are existing encounters, get the latest and increment by one to get the new encounter
                            encounterNumber+=1;

                        }else {

                        }

                        List<PatientAppointment> appointments = baseDatabase.appointmentModelDao().getAppointmentByEncounterNumberAndPatientID(encounterNumber, currentPatient.getPatientId());
                        if (appointments.size() > 0){
                            dose.setAppointment(appointments.get(0));
                        }

                    }
                    dose.setTbPatient(patient);
                    dose.setEncounters(encounters);
                    dose.setCurrentEncounterNumber(encounterNumber);

                    doseList.add(dose);
                    emitter.onNext(dose);
                }
                emitter.onComplete();
            }catch (Exception e){
                emitter.onError(e);
            }
        });

        observable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());


        /**
         * An observer to observe on the information as they are being emmited by the observable
         */
        io.reactivex.Observer<Dose> observer = new io.reactivex.Observer<Dose>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Dose dose) {
                List<TbEncounters> tbEncounters = dose.encounters;

                Log.d("human", "Observed encounters size "+tbEncounters.size());

                View columnView = LayoutInflater.from(TbClientDetailsActivity.this).inflate(R.layout.previous_encounters_title_view, previousEncountersTitleTableRow, false);
                previousEncountersTitleTableRow.addView(columnView);

                TextView title = columnView.findViewById(R.id.title);

                //Check to see if the encounter is the current encounter
                if (dose.isCurrentDose()){

                    selectedEncounterAppointment = dose.getAppointment();
                    encounterNumber = dose.getCurrentEncounterNumber();
                    int effectiveEncounterNumber = encounterNumber;
                    PatientAppointment effectiveSelectedAppointment = selectedEncounterAppointment;

                    title.setText(getResources().getString(R.string.current_dose));
                    columnView.setBackground(getResources().getDrawable(R.drawable.border_fill_button));

                    //set the value of current encounter that the user is issuing
                    currentEncounterNumber.setText(encounterNumber+"");

                    //Set the encounter date of issue to today's date
                    encounterDate.setText(BaseActivity.simpleDateFormat.format(new Date()));

                    //Check if this is the 1st or 3rd visit and test type is 1 only then display the sputum for afb input elements
                    if (currentTbPatient.getTestType() == 1){
                        if (effectiveEncounterNumber == 1 || effectiveEncounterNumber == 3 || effectiveEncounterNumber == 6){
                            if (effectiveEncounterNumber == 1)
                                finishedPreviousMonthLayout.setVisibility(View.INVISIBLE);
                        }else {
                            makohoziEncounterWrap.setVisibility(View.INVISIBLE);
                        }
                    }

                    //Check if the encounterNumber is greater than 6 then start showing the medications results view
                    if (effectiveEncounterNumber >= 6){
                        finishedTreatmentToggle.setVisibility(View.VISIBLE);
                        finishedTreatmentTitle.setVisibility(View.VISIBLE);
                    }else {
                        finishedTreatmentToggle.setVisibility(View.GONE);
                        finishedTreatmentTitle.setVisibility(View.GONE);
                    }

                    //Set the date of the appointment associated with this encounter
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(effectiveSelectedAppointment == null ? calendar.getTimeInMillis() : effectiveSelectedAppointment.getAppointmentDate());
                    encounterAppointmentDate.setText(BaseActivity.simpleDateFormat.format(calendar.getTime()));

                    if (tbEncounters.size() > 0){
                        emptyPreviousMonthEncounter.setVisibility(View.GONE);
                        previousEncountersRecycler.setVisibility(View.VISIBLE);
                        adapter.updateData(tbEncounters);
                    }else {
                        previousEncountersRecycler.setVisibility(View.GONE);
                        emptyPreviousMonthEncounter.setVisibility(View.VISIBLE);
                    }

                }else {
                    title.setText(getResources().getString(R.string.previous_dose));
                }

                columnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        columnView.setBackground(getResources().getDrawable(R.drawable.border_fill_button));


                        if (tbEncounters.size() > 0){
                            emptyPreviousMonthEncounter.setVisibility(View.GONE);
                            previousEncountersRecycler.setVisibility(View.VISIBLE);
                            adapter.updateData(tbEncounters);
                        }else {
                            previousEncountersRecycler.setVisibility(View.GONE);
                            emptyPreviousMonthEncounter.setVisibility(View.VISIBLE);
                        }

                        for (int j = 0; j< doseList.size(); j++){
                            if (doseList.get(j) != dose){
                                previousEncountersTitleTableRow.getChildAt(j).setBackground(getResources().getDrawable(R.drawable.border_outline));
                            }
                        }
                    }
                });

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

        //Creating the subscription
        observable.subscribe(observer);

    }


    //START..::..Background Activities

    class GetTbPatientByPatientID extends AsyncTask<String, Void, TbPatient>{

        AppDatabase database;

        GetTbPatientByPatientID(AppDatabase db){
            this.database = db;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected TbPatient doInBackground(String... strings) {

            TbPatient patient = database.tbPatientModelDao().getTbPatientCurrentOnTreatment(strings[0]);
            currentTbPatient = patient;

            return patient;
        }

        @Override
        protected void onPostExecute(TbPatient tbPatient) {
            super.onPostExecute(tbPatient);

            if (tbPatient != null){

                LiveData<List<TbPatient>> tbPatients = baseDatabase.tbPatientModelDao().getTbPatientIdsByHealthFacilityId(currentPatient.getPatientId());
                tbPatients.observe(TbClientDetailsActivity.this, new Observer<List<TbPatient>>() {
                    @Override
                    public void onChanged(@Nullable List<TbPatient> tbPatients) {
                        getEncounterRecords(tbPatients);
                        //new GetPreviousEncounters(tbPatients).execute();
                    }
                });


                int testType = tbPatient.getTestType();
                clickBlocker.setVisibility(View.VISIBLE);

                //Check to see the testType variable has value
                if (testType > 0){

                    testTypeToggle.setCheckedTogglePosition(testType-1);
                    testTypeToggle.setFocusableInTouchMode(false);

                    switch (testType){
                        case 1:
                            monthOneMakohoziSpinner.setVisibility(View.VISIBLE);
                            monthOneMakohoziSpinner.setEnabled(false);
                            for (int i=0; i<tbTypesFirstEncounter.length; i++){
                                if (tbPatient.getMakohozi().equals(tbTypesFirstEncounter[i])){
                                    monthOneMakohoziSpinner.setSelection(i);
                                }
                            }
                            break;
                        case 2:
                            makohoziSpinner.setVisibility(View.GONE);
                            break;
                        case 3:
                            makohoziSpinner.setVisibility(View.GONE);
                            otherTestValue.setText(tbPatient.getOtherTestDetails());
                            break;
                    }

                }

                /*
                get all the appointments to allow the user to select from the list of appointments which one
                is being attended with this particular visit
                Appointments should be the one that have not been attended to with status Pending
                TODO -> Remove this
                */
                getUnattendedAppointments();

                patientWeight.setText(tbPatient.getWeight()+"" == null ? "" : tbPatient.getWeight()+"");
                for (int i=0; i<treatmentTypes.length; i++){
                    if (treatmentTypes[i].equals(tbPatient.getTreatment_type())){
                        matibabuSpinner.setSelection(i+1);
                    }
                }

                if (patientNew){
                    matibabuSpinner.setEnabled(true);
                    clickBlocker.setVisibility(View.GONE);
                }else {
                    matibabuSpinner.setEnabled(false);
                    clickBlocker.setVisibility(View.VISIBLE);
                }

            }

        }
    }

    class SaveTbPatientTask extends AsyncTask<TbPatient, Void, Void> {

        AppDatabase database;

        SaveTbPatientTask(AppDatabase db){
            this.database = db;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
            dialog.setMessage("Loading. Saving patient data...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (activityCanExit){
                Intent intent = new Intent(context, TbClientListActivity.class);
                startActivity(intent);
                finish();
            }else {
                activityCanExit = true;
            }
        }

        @Override
        protected Void doInBackground(TbPatient... tbPatients) {
            database.tbPatientModelDao().updateTbPatient(tbPatients[0]);
            return null;
        }

    }

    class SaveEncounters extends AsyncTask<TbEncounters, Void, Void> {

        AppDatabase database;
        TbEncounters currentEncounter;
        int mEncMonth = 0;

        SaveEncounters(AppDatabase db){
            this.database = db;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Loading. Saving Encounters...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(TbEncounters... encounters) {

            currentEncounter = encounters[0];

            //Save current encounter
            Log.d("Billions", "Saving current Encounter for month "+encounters[0].getEncounterNumber());
            database.tbEncounterModelDao().addEncounter(currentEncounter);

            PostOffice postOffice = new PostOffice();
            postOffice.setPost_id(currentEncounter.getLocalID());
            postOffice.setPost_data_type(POST_DATA_TYPE_ENCOUNTER);
            postOffice.setSyncStatus(ENTRY_NOT_SYNCED);

            database.postOfficeModelDao().addPostEntry(postOffice);

            //Save previous month medication status
            int previousEncounter = currentEncounter.getEncounterNumber() - 1;
            mEncMonth = currentEncounter.getEncounterNumber();
            boolean previousMonthStatus = currentEncounter.isHasFinishedPreviousMonthMedication(); //Previous month status entered this month

            List<TbEncounters> encounters1 = database.tbEncounterModelDao().getMonthEncounter(previousEncounter, currentTbPatient.getTbPatientId());

            if (encounters1.size() > 0){
                encounters1.get(0).setMedicationStatus(previousMonthStatus);

                Calendar calendar = Calendar.getInstance();
                long today = calendar.getTimeInMillis();
                encounters1.get(0).setMedicationDate(today);

                database.tbEncounterModelDao().updatePreviousMonthMedicationStatus(encounters1.get(0));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new GenerateNextAppointment(baseDatabase).execute(currentEncounter.getEncounterNumber());
        }

    }

    class GenerateNextAppointment extends AsyncTask<Integer, Void, Void>{

        int encNumber;
        long now;
        AppDatabase database;

        GenerateNextAppointment(AppDatabase db){
            this.database = db;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (activityCanExit){
                Intent intent = new Intent(context, TbClientListActivity.class);
                startActivity(intent);
                finish();
            }else {
                activityCanExit = true;
            }
        }

        @Override
        protected Void doInBackground(Integer... encounterNumber) {

            encNumber = encounterNumber[0] == null ? 0 : encounterNumber[0];

            // Generate a new appointment for this patient and set encounter number to @encNumber
            Calendar calendar = Calendar.getInstance();
            now = calendar.getTimeInMillis();

            PatientAppointment nextAppointment = new PatientAppointment();
            calendar.add(DATE, 30); //Adding 30 days to the next Appointment
            nextAppointment.setAppointmentDate(calendar.getTimeInMillis());
            nextAppointment.setEncounterNumber(encNumber);
            nextAppointment.setStatus(STATUS_PENDING_VAL);

            long range = 1234567L;
            Random r = new Random();
            long number = (long)(r.nextDouble()*range);

            nextAppointment.setAppointmentID(number);
            nextAppointment.setPatientID(currentPatient.getPatientId());
            nextAppointment.setAppointmentType(2);

            database.appointmentModelDao().addAppointment(nextAppointment);

            /*
            Appointment is the last thing so send patient data to PostOffice

            UPDATED: This sends patient object to server every time an encounter and hence an appointment
            is saved, remove this
            */

            /*
            PostOffice postOffice = new PostOffice();
            postOffice.setPost_id(currentPatient.getPatientId());
            postOffice.setPost_data_type(POST_DATA_TYPE_PATIENT);
            postOffice.setSyncStatus(ENTRY_NOT_SYNCED);

            database.postOfficeModelDao().addPostEntry(postOffice);

            */

            return null;
        }

    }

    class EnrollPatientToTbClinic extends AsyncTask<String, Void, Void>{

        AppDatabase database;
        Patient patient;
        TbPatient tbPatient;

        EnrollPatientToTbClinic(AppDatabase db){
            this.database = db;
        }

        @Override
        protected Void doInBackground(String... strings) {
            currentPatientId = strings[0];

            //Update patient to currently on TB Clinic
            patient = database.patientModel().getPatientById(currentPatientId);
            patient.setCurrentOnTbTreatment(true);
            patient.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
            //TODO : handle CTC Number input at the clinic
            database.patientModel().updatePatient(patient);

            PostOffice patientPost = new PostOffice();
            patientPost.setPost_id(patient.getPatientId());
            patientPost.setPost_data_type(POST_DATA_TYPE_PATIENT);
            patientPost.setSyncStatus(ENTRY_NOT_SYNCED);
            database.postOfficeModelDao().addPostEntry(patientPost);

            //Create a new Tb Patient and add to post office
            tbPatient = new TbPatient();
            tbPatient.setTbPatientId(new Random().nextLong());
            tbPatient.setHealthFacilityPatientId(Long.parseLong(patient.getPatientId()));
            tbPatient.setTempID(UUID.randomUUID()+"");
            tbPatient.setTreatmentStatus(1);
            database.tbPatientModelDao().addPatient(tbPatient);
            currentTbPatient = tbPatient;

            PostOffice tbPatientPost = new PostOffice();
            tbPatientPost.setPost_id(String.valueOf(tbPatient.getTbPatientId()));
            tbPatientPost.setPost_data_type(POST_DATA_TYPE_TB_PATIENT);
            tbPatientPost.setSyncStatus(ENTRY_NOT_SYNCED);
            database.postOfficeModelDao().addPostEntry(tbPatientPost);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (saveTestData()){
                Log.d("saveTestData", "Saved test data!");
                if (saveTreatmentData()){
                    Log.d("saveTreatmentData", "Saved treatment data!");
                    new SaveTbPatientTask(baseDatabase).execute(currentTbPatient);
                }
            }

        }
    }

    //END..::..Background Activities

    //START..::..Utility methods

    private void clearFields(){
        makohoziSpinner.setSelection(0);
        makohoziSpinner.setEnabled(true);
        medicationStatusCheckbox.setChecked(false);
        medicationStatusCheckbox.setEnabled(true);
        monthlyPatientWeightEt.setText("");
        monthlyPatientWeightEt.setEnabled(true);
        medicationStatusTitle.setText("Amemaliza Dawa Za Mwezi Uliopita Kikamilifu");

        appointmentsSpinner.setSelection(0);
        appointmentsSpinner.setEnabled(true);

    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    void toastThis (String toastString){
        Toast.makeText(TbClientDetailsActivity.this, toastString, Toast.LENGTH_LONG).show();
    }

    //END..::..Utility methods

    //START..::..Inline Classes
    class PreviousEncountersRecyclerAdapter extends RecyclerView.Adapter<PreviousEncountersRecyclerAdapter.ViewHolder> {

        private List<TbEncounters> mData = Collections.emptyList();
        private LayoutInflater mInflater;
        //private ItemClickListener mClickListener;

        // data is passed into the constructor
        PreviousEncountersRecyclerAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.previous_encounters_list_item, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TbEncounters encounter = mData.get(position);
            holder.month.setText(encounter.getEncounterNumber()+"");
            holder.weight.setText(encounter.getWeight());
            holder.spatum.setText(encounter.getMakohozi());

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(encounter.getMedicationDate());
            Date date = cal.getTime();

            holder.previousEncounterDate.setText(BaseActivity.simpleDateFormat.format(date));
            if (encounter.isMedicationStatus()){
                holder.finishedMedications.setText(context.getResources().getString(R.string.finished));
            }else {
                holder.finishedMedications.setText(context.getResources().getString(R.string.didnt_finished));
                holder.finishedMedications.setTextColor(context.getResources().getColor(R.color.red_500));
            }
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView month, weight, spatum, finishedMedications, previousEncounterDate;

            ViewHolder(View itemView) {
                super(itemView);
                month = itemView.findViewById(R.id.previous_month);
                weight = itemView.findViewById(R.id.previous_weight);
                spatum = itemView.findViewById(R.id.previous_spatum);
                finishedMedications = itemView.findViewById(R.id.finished_previous_medications);
                previousEncounterDate = itemView.findViewById(R.id.previous_encounter_date);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        // convenience method for getting data at click position
        TbEncounters getItem(int id) {
            return mData.get(id);
        }

        private void updateData(List<TbEncounters> updatedData){

            EncountersDiffCallback encountersDiffCallback = new EncountersDiffCallback(this.mData, updatedData);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(encountersDiffCallback);

            this.mData = Collections.emptyList();
            this.mData = updatedData;

            result.dispatchUpdatesTo(this);
        }

    }

    class Dose {

        private boolean isCurrentDose = false;

        private TbPatient tbPatient;

        private int currentEncounterNumber = 0;

        private PatientAppointment appointment;

        private List<TbEncounters> encounters;

        public Dose(){}

        public TbPatient getTbPatient() {
            return tbPatient;
        }

        public void setTbPatient(TbPatient tbPatient) {
            this.tbPatient = tbPatient;
        }

        public List<TbEncounters> getEncounters() {
            return encounters;
        }

        public void setEncounters(List<TbEncounters> encounters) {
            this.encounters = encounters;
        }

        public boolean isCurrentDose() {
            return isCurrentDose;
        }

        public void setCurrentDose(boolean currentDose) {
            isCurrentDose = currentDose;
        }

        public int getCurrentEncounterNumber() {
            return currentEncounterNumber;
        }

        public void setCurrentEncounterNumber(int currentEncounterNumber) {
            this.currentEncounterNumber = currentEncounterNumber;
        }

        public PatientAppointment getAppointment() {
            return appointment;
        }

        public void setAppointment(PatientAppointment appointment) {
            this.appointment = appointment;
        }
    }

    //END..::..Inline Classes
}
