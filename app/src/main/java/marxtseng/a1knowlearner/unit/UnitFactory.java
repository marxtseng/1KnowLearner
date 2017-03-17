package marxtseng.a1knowlearner.unit;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by marx on 06/02/2017.
 */

public class UnitFactory {

    public static UnitFragment createInstance(Map<String, String> item) {
        Bundle bundle = new Bundle();
        bundle.putString("UQID", item.get("uqid"));

        switch (item.get("unit_type")) {
            case "video":
                UnitFragment videoFragment = new VideoFragment();
                videoFragment.setArguments(bundle);
                return videoFragment;
            case "web":
                UnitFragment webFragment = new WebFragment();
                webFragment.setArguments(bundle);
                return webFragment;
            case "embed":
                UnitFragment embedFragment = new EmbedFragment();
                embedFragment.setArguments(bundle);
                return embedFragment;
            case "quiz":
                UnitFragment quizFragment = new QuizFragment();
                quizFragment.setArguments(bundle);
                return quizFragment;
            case "exam":
                UnitFragment examFragment = new ExamFragment();
                examFragment.setArguments(bundle);
                return examFragment;
            case "qa":
                UnitFragment qaFragment = new QAFragment();
                qaFragment.setArguments(bundle);
                return qaFragment;
            case "poll":
                UnitFragment pollFragment = new PollFragment();
                pollFragment.setArguments(bundle);
                return pollFragment;
            case "draw":
                UnitFragment drawFragment = new DrawFragment();
                drawFragment.setArguments(bundle);
                return drawFragment;
            case "gdrive-file":
                UnitFragment driveFragment = new DriveFragment();
                driveFragment.setArguments(bundle);
                return driveFragment;
            default:
                return null;
        }
    }
}
