package Utils;

import Backend.CopyExecutables;
import Enums.Mode;
import Enums.OS;
import Enums.Program;
import Preferences.AppSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static Enums.Program.YT_DLP;

public class Environment {
    private static MessageBroker msgBroker = Environment.getMessageBroker();

    public static void initializeEnvironment() {
        msgBroker.msgLogInfo("OS : " + OS.getOSName());
        String ytDlpExecName = OS.isWindows() ? "yt-dlp.exe" : OS.isMac() ? "yt-dlp_macos" : "yt-dlp";
        String spotDLExecName = OS.isWindows() ? "spotdl_win.exe" : OS.isMac() ? "spotdl_macos" : "spotdl_linux";
        String driftyFolderPath = OS.isWindows() ? Paths.get(System.getenv("LOCALAPPDATA"), "Drifty").toAbsolutePath().toString() : Paths.get(System.getProperty("user.home"), ".drifty").toAbsolutePath().toString();
        Program.setYtDlpExecutableName(ytDlpExecName);
        Program.setSpotdlExecutableName(spotDLExecName);
        Program.setDriftyPath(driftyFolderPath);
        CopyExecutables copyExecutables = new CopyExecutables();
        try {
            copyExecutables.copyExecutables(new String[]{ytDlpExecName, spotDLExecName});
        } catch (IOException e) {
            msgBroker.msgInitError("Failed to copy yt-dlp! " + e.getMessage());
            msgBroker.msgInitError("Failed to copy spotDL! " + e.getMessage());
        }
        boolean ytDLPExists = Files.exists(Paths.get(driftyFolderPath, ytDlpExecName));
        Mode previousMode = Mode.getMode();
        Mode.setUpdateMode();
        if (!isDriftyUpdated()) {
            if (Utility.isUpdateAvailable()) {
                msgBroker.msgUpdateInfo("Updating Drifty...");
                Utility.updateDrifty(previousMode);
            }
        }
        if (ytDLPExists && !isYtDLPUpdated()) {
            checkAndUpdateYtDlp();
        }
        Mode.setMode(previousMode);
        File folder = new File(driftyFolderPath);
        if (!folder.exists()) {
            try {
                Files.createDirectory(folder.toPath());
                msgBroker.msgInitInfo("Created Drifty folder : " + driftyFolderPath);
            } catch (IOException e) {
                msgBroker.msgInitError("Failed to create Drifty folder: " + driftyFolderPath + " - " + e.getMessage());
            }
        } else {
            msgBroker.msgInitInfo("Drifty folder already exists : " + driftyFolderPath);
        }
    }

    public static void setMessageBroker(MessageBroker messageBroker) {
        Environment.msgBroker = messageBroker;
    }

    public static void checkAndUpdateYtDlp() {
        msgBroker.msgInitInfo("Checking for component (yt-dlp) update ...");
        String command = Program.get(YT_DLP);
        ProcessBuilder ytDlpUpdateProcess = new ProcessBuilder(command, "-U");
        ytDlpUpdateProcess.inheritIO();
        try {
            Process ytDlpUpdateTask = ytDlpUpdateProcess.start();
            ytDlpUpdateTask.waitFor();
            AppSettings.SET.lastDLPUpdateTime(System.currentTimeMillis());
        } catch (IOException e) {
            msgBroker.msgInitError("Failed to update yt-dlp! " + e.getMessage());
        } catch (InterruptedException e) {
            msgBroker.msgInitError("Component (yt-dlp) update process was interrupted! " + e.getMessage());
        }
    }

    public static boolean isYtDLPUpdated() {
        final long oneDay = 1000 * 60 * 60 * 24; // Value of one day (24 Hours) in milliseconds
        long timeSinceLastUpdate = System.currentTimeMillis() - AppSettings.GET.lastDLPUpdateTime();
        return timeSinceLastUpdate <= oneDay;
    }
    
    public static boolean isDriftyUpdated() {
        final long oneDay = 1000 * 60 * 60 * 24; // Value of one day (24 Hours) in milliseconds
        long timeSinceLastUpdate = System.currentTimeMillis() - AppSettings.GET.lastDriftyUpdateTime();
        return timeSinceLastUpdate <= oneDay;
    }

    public static MessageBroker getMessageBroker() {
        return msgBroker;
    }
}
