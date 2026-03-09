package com.floyd.sdk.infrastructure.git;

import com.floyd.sdk.types.utils.RandomStringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GitCommand {

    private final static Logger logger = LoggerFactory.getLogger(GitCommand.class);

    private final String githubLogReviewUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public GitCommand(String githubLogReviewUri, String githubToken, String project, String branch, String author, String message) {
        this.githubLogReviewUri = githubLogReviewUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    public String diff() throws IOException, InterruptedException {
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--no-merges", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));

        StringBuilder diffCode = new StringBuilder();
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();
        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failure to get diff, exit code: " + exitCode);
        }
        return diffCode.toString();
    }

    // https://github.com/burn1t/openai-code-review-log
    public String commitAndPush(String recommend) throws GitAPIException, IOException {
        // 克隆仓库至 repo 文件夹
        Git git = Git.cloneRepository()
                .setURI(githubLogReviewUri + ".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        // 创建日期标识的文件夹，并创建日志文件
        String logFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File logFolder = new File("repo/" + logFolderName);
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }

        String fileName = project + "-" + branch + "-" + author + "-" + System.currentTimeMillis() + "-" + RandomStringUtils.generateRandomString(5);
        File file = new File(logFolder, fileName);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(recommend);
        }

        // 提交并推送日志文件至评审日志仓库
        git.add().addFilepattern(logFolderName + "/" + fileName).call();
        git.commit().setMessage("docs: Add code review new file").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();
        logger.info("openai-code-review commit and push done! {}", fileName);

        return githubLogReviewUri + "/blob/master/" + logFolderName + "/" + fileName;
    }

    public String getGithubLogReviewUri() {
        return githubLogReviewUri;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }
}
