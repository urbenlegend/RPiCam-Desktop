package com.rpicam.detection;

import com.rpicam.cameras.ByteBufferImage;
import com.rpicam.config.ClassifierConfig;
import com.rpicam.config.ConfigService;
import com.rpicam.config.OCVClassifierConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleThreadClassifierService implements ClassifierService {
    private static SingleThreadClassifierService instance;

    private ConfigService configService;

    private List<Classifier> classifiers = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService classifierPool = Executors.newSingleThreadExecutor();

    private SingleThreadClassifierService() {
        // Load classifiers from config
        configService = ServiceLoader.load(ConfigService.class).findFirst().get();
        var configRoot = configService.getConfig();
        for (var conf : configRoot.classifiers) {
            if (conf instanceof OCVClassifierConfig) {
                var ocvConf = (OCVClassifierConfig) conf;
                var classifier = new OCVClassifier(ocvConf.path, ocvConf.title, ocvConf.color, ocvConf.scaleFactor, ocvConf.minNeighbors, ocvConf.minSizeFactor, ocvConf.gpu);
                classifiers.add(classifier);
            }
        }
    }

    public static SingleThreadClassifierService provider() {
        if (instance == null) {
            instance = new SingleThreadClassifierService();
        }
        return instance;
    }

    @Override
    public void shutdown() {
        classifierPool.shutdownNow();

        // Save classifiers to config
        var configRoot = configService.getConfig();
        ArrayList<ClassifierConfig> classifierConfs = new ArrayList<>();
        for (var classifier : classifiers) {
            classifierConfs.add(classifier.toConfig());
        }
        configRoot.classifiers = new ClassifierConfig[classifierConfs.size()];
        classifierConfs.toArray(configRoot.classifiers);
    }

    @Override
    public void addClassifier(Classifier c) {
        classifiers.add(c);
    }

    @Override
    public void removeClassifier(Classifier c) {
        classifiers.remove(c);
    }

    @Override
    public List<ClassifierResult> submit(ByteBufferImage image) throws InterruptedException {
        // Create classifier jobs
        var classifierJobs = classifiers.stream()
                .map(c -> (Callable<List<ClassifierResult>>) () -> c.apply(image))
                .collect(Collectors.toList());

        // Feed jobs into classifier executor and wait for results
        var classifierResults = classifierPool.invokeAll(classifierJobs).stream()
                .flatMap(resultFuture -> {
                    try {
                        return resultFuture.get().stream();
                    } catch (InterruptedException | ExecutionException e) {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        return classifierResults;
    }
}
