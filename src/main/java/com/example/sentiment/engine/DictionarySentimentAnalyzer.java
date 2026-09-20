package com.example.sentiment.engine;

import com.example.sentiment.vo.SentimentResult;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 词典法情感分析引擎（纯 Java）
 *
 * 算法流程：
 * 1. jieba 分词，过滤停用词；
 * 2. 遍历每个情感词，向前回看最多 WINDOW 个词：
 *    - 出现否定词 → 极性反转（乘 -1）；
 *    - 出现程度副词 → 强度乘以倍率；
 * 3. 累加所有情感词贡献得到总分；
 * 4. 按阈值映射为 正向/中性/负向，并输出启发式置信度与命中的关键词。
 */
@Component
public class DictionarySentimentAnalyzer implements SentimentAnalyzer {

    /** 向前回看窗口大小 */
    private static final int WINDOW = 3;

    private final JiebaSegmenter segmenter = new JiebaSegmenter();

    @Autowired
    private EmotionDictManager dictManager;

    @Value("${sentiment.dict.positive-threshold}")
    private double positiveThreshold;

    @Value("${sentiment.dict.negative-threshold}")
    private double negativeThreshold;

    @Override
    public String engineName() {
        return "dictionary";
    }

    @Override
    public SentimentResult analyze(String text) {
        List<String> tokens = segment(text);

        double score = 0.0;
        double absContributionSum = 0.0;
        List<String> keywords = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            String word = tokens.get(i);
            double[] sw = dictManager.getSentiment(word);
            if (sw == null) {
                continue;
            }
            double polarity = sw[0];
            double weight = sw[1];
            double factor = 1.0;
            boolean negated = false;

            // 向前回看窗口内是否有否定词 / 程度副词
            for (int j = Math.max(0, i - WINDOW); j < i; j++) {
                String prev = tokens.get(j);
                if (dictManager.isNegation(prev)) {
                    negated = !negated;
                }
                Double deg = dictManager.getDegree(prev);
                if (deg != null) {
                    factor *= deg;
                }
            }

            double contribution = polarity * weight * factor * (negated ? -1 : 1);
            score += contribution;
            absContributionSum += Math.abs(contribution);
            keywords.add(word);
        }

        SentimentResult result = new SentimentResult();
        result.setText(text);
        result.setScore(round(score));
        result.setKeywords(keywords);
        result.setEngineType(engineName());

        if (score > positiveThreshold) {
            result.setSentimentType(1);
            result.setSentimentLabel("正向");
        } else if (score < negativeThreshold) {
            result.setSentimentType(-1);
            result.setSentimentLabel("负向");
        } else {
            result.setSentimentType(0);
            result.setSentimentLabel("中性");
        }

        // 启发式置信度：无情感词为 0，否则 min(1, |score|)
        double confidence = absContributionSum <= 0 ? 0.0 : Math.min(1.0, Math.abs(score));
        result.setConfidence(round(confidence));

        return result;
    }

    private List<String> segment(String text) {
        List<String> tokens = new ArrayList<>();
        List<SegToken> segTokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        for (SegToken token : segTokens) {
            String word = token.word.trim();
            if (word.isEmpty()) {
                continue;
            }
            if (dictManager.isStopword(word)) {
                continue;
            }
            tokens.add(word);
        }
        return tokens;
    }

    private double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
