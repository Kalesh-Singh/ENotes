package com.kaleshsingh.e_notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    public static final String NOTE_POSITION = "com.kaleshsingh.e_note.NOTE_POSITION";
    public static final String ORIGINAL_COURSE_ID = "com.kaleshsingh.e_notes.ORIGINAL_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.kaleshsingh.e_notes.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.kaleshsingh.e_notes.ORIGINAL_NOTE_TEXT";

    public static final int POSITION_NOT_SET = -1;
    private NoteInfo note;
    private boolean isNewNote;
    private Spinner spinnerCourses;
    private EditText textNoteTitle;
    private EditText textNoteText;
    private int notePosition;
    private boolean isCancelling;
    private String originalNoteCourseId;
    private String originalNoteTitle;
    private String originalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerCourses = (Spinner) findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        textNoteTitle = (EditText) findViewById(R.id.text_note_title);
        textNoteText = (EditText) findViewById(R.id.text_note_text);

        if (!isNewNote) {
            displayNote(spinnerCourses, textNoteTitle, textNoteText);
        }

        Log.d(TAG, "onCreate");

    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        originalNoteCourseId = savedInstanceState.getString(ORIGINAL_COURSE_ID);
        originalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        originalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalNoteValues() {

        if (isNewNote) {
            return;
        }
        originalNoteCourseId = note.getCourse().getCourseId();
        originalNoteTitle = note.getTitle();
        originalNoteText = note.getText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position: " + notePosition);
            if (isNewNote) {
                DataManager.getInstance().removeNote(notePosition);
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }

        Log.d(TAG, "onPause");
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteCourseId);
        note.setCourse(course);
        note.setTitle(originalNoteTitle);
        note.setText(originalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_COURSE_ID,   originalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, originalNoteText);

    }

    private void saveNote() {
        note.setCourse((CourseInfo) spinnerCourses.getSelectedItem());
        note.setTitle(textNoteTitle.getText().toString());
        note.setText(textNoteText.getText().toString());
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(note.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(note.getTitle());
        textNoteText.setText(note.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        notePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        isNewNote = notePosition == POSITION_NOT_SET;
        if(isNewNote) {
            createNewNote();
        }
        Log.i(TAG, "notePosition" + notePosition);
        note = DataManager.getInstance().getNotes().get(notePosition);
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        notePosition = dm.createNewNote();
//        note = dm.getNotes().get(notePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            isCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo)  spinnerCourses.getSelectedItem();
        String subject = textNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + textNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}
