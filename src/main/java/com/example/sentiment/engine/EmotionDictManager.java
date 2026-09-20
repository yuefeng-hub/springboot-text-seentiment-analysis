package com.example.sentiment.engine;

import com.example.sentiment.entity.SentimentDict;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * 情感词典管理器
 * 负责加载 情感词/否定词/程度副词/停用词，并支持运行时重载（管理员新增词典后调用 reload）
 */
@Component
public class EmotionDictManager {

    /** 情感词 -> [极性(1/-1), 权重] */
    private final Map<String, double[]> sentimentWords = new HashMap<>();

    /** 程度副词 -> 倍率 */
    private final Map<String, Double> degreeWords = new HashMap<>();

    /** 否定词集合 */
    private final Set<String> negationWords = new HashSet<>();

    /** 停用词集合 */
    private final Set<String> stopwords = new HashSet<>();

    @Value("${sentiment.dict.sentiment-dict-path}")
    private String sentimentDictPath;

    @Value("${sentiment.dict.negation-path}")
    private String negationPath;

    @Value("${sentiment.dict.degree-path}")
    private String degreePath;

    @Value("${sentiment.dict.stopword-path}")
    private String stopwordPath;

    @PostConstruct
    public void init() {
        loadSentimentDict();
        loadNegation();
        loadDegree();
        loadStopwords();
    }

    /**
     * 重新加载：内置 CSV + 数据库自定义词合并。
     * 管理员增删改词典后调用，使新词实时生效。
     */
    public synchronized void reload(List<SentimentDict> customDicts) {
        loadSentimentDict();
        if (customDicts != null) {
            for (SentimentDict d : customDicts) {
                if (d.getWord() == null || d.getWord().trim().isEmpty()) {
                    continue;
                }
                int polarity = d.getPolarity() == null ? 0 : d.getPolarity();
                double weight = d.getWeight() == null ? 1.0 : d.getWeight();
                sentimentWords.put(d.getWord().trim(), new double[]{polarity, weight});
            }
        }
    }

    private void loadSentimentDict() {
        sentimentWords.clear();
        for (String line : readLines(sentimentDictPath)) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length < 2) {
                continue;
            }
            String word = parts[0].trim();
            int polarity;
            double weight = 1.0;
            try {
                polarity = Integer.parseInt(parts[1].trim());
                if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                    weight = Double.parseDouble(parts[2].trim());
                }
            } catch (NumberFormatException e) {
                continue;
            }
            sentimentWords.put(word, new double[]{polarity, weight});
        }
    }

    private void loadNegation() {
        negationWords.clear();
        for (String line : readLines(negationPath)) {
            if (!line.isEmpty() && !line.startsWith("#")) {
                negationWords.add(line.trim());
            }
        }
    }

    private void loadDegree() {
        degreeWords.clear();
        for (String line : readLines(degreePath)) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length < 2) {
                continue;
            }
            try {
                degreeWords.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
            } catch (NumberFormatException e) {
                // 忽略格式错误行
            }
        }
    }

    private void loadStopwords() {
        stopwords.clear();
        for (String line : readLines(stopwordPath)) {
            if (!line.isEmpty() && !line.startsWith("#")) {
                stopwords.add(line.trim());
            }
        }
    }

    private java.util.List<String> readLines(String path) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            throw new IllegalStateException("词典文件加载失败: " + path, e);
        }
        return lines;
    }

    // ================== 对外查询方法 ==================

    /** 返回情感词 [极性, 权重]，不是情感词返回 null */
    public double[] getSentiment(String word) {
        return sentimentWords.get(word);
    }

    public boolean isSentimentWord(String word) {
        return sentimentWords.containsKey(word);
    }

    /** 程度副词倍率，不是程度词返回 null */
    public Double getDegree(String word) {
        return degreeWords.get(word);
    }

    public boolean isNegation(String word) {
        return negationWords.contains(word);
    }

    public boolean isStopword(String word) {
        return stopwords.contains(word);
    }
}
