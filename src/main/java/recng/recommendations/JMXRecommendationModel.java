package recng.recommendations;

public class JMXRecommendationModel implements JMXRecommendationModelMBean {

    private final RecommendationModel model;

    public JMXRecommendationModel(RecommendationModel model) {
        this.model = model;
    }

    @Override
    public String getStats() {
        return model.toString();
    }

}
