package recng.filter;

public enum ComparisonOperator {

    LT {

        @Override
        public <T extends Comparable<T>> boolean matches(T value, T matchAgainst) {
            return value.compareTo(matchAgainst) < 0;
        }

    },
    LTEQ {

        @Override
        public <T extends Comparable<T>> boolean matches(T value, T matchAgainst) {
            return value.compareTo(matchAgainst) <= 0;
        }
    },
    EQ {

        @Override
        public <T extends Comparable<T>> boolean matches(T value, T matchAgainst) {
            return value.compareTo(matchAgainst) == 0;
        }
    },
    NEQ {

        @Override
        public <T extends Comparable<T>> boolean matches(T value, T matchAgainst) {
            return value.compareTo(matchAgainst) != 0;
        }
    },
    GTEQ {

        @Override
        public <T extends Comparable<T>> boolean matches(T value, T matchAgainst) {
            return value.compareTo(matchAgainst) >= 0;
        }
    },
    GT {

        @Override
        public <T extends Comparable<T>> boolean matches(T value, T matchAgainst) {
            return value.compareTo(matchAgainst) > 0;
        }
    };


    public abstract <T extends Comparable<T>> boolean matches(T value, T matchAgainst);
}
