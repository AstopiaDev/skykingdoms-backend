package net.skykingdoms.backend.base.util.time;

import java.util.EnumMap;

public abstract class DurationFormatter {

    private abstract static class WordDurationFormatter extends DurationFormatter {

        public abstract String format(long value, DurationField field);

        @Override
        public void append(StringBuilder builder, long value, DurationField field) {
            if (value > 0) {
                builder.append(value).append(format(value, field)).append(' ');
            }
        }

        @Override
        public void finish(StringBuilder builder) {
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
        }

    }

    public static final DurationFormatter SIMPLE = new DurationFormatter() {

        @Override
        public void append(StringBuilder builder, long value, DurationField field) {
            if (value < 10) {
                builder.append('0');
            }

            builder.append(value).append(':');
        }

        @Override
        public void finish(StringBuilder builder) {
            if (builder.length() > 0) {
                // cut last ':'
                builder.setLength(builder.length() - 1);
            }
        }

    };

    public static final DurationFormatter EN = new WordDurationFormatter() {

        private final EnumMap<DurationField, String[]> values = new EnumMap<>(DurationField.class);

        {
            values.put(DurationField.NANO, new String[]{" nanosecond"});
            values.put(DurationField.MICRO, new String[]{" microsecond"});
            values.put(DurationField.MILLI, new String[]{" millisecond"});
            values.put(DurationField.SEC, new String[]{" second"});
            values.put(DurationField.MIN, new String[]{" minute"});
            values.put(DurationField.HOUR, new String[]{" hour"});
            values.put(DurationField.DAY, new String[]{" day"});
            values.put(DurationField.WEEK, new String[]{" week"});
            values.put(DurationField.MONTH, new String[]{" month"});
            values.put(DurationField.YEAR, new String[]{" year"});
        }

        @Override
        public String format(long value, DurationField field) {
            String[] strings = this.values.get(field);

            return value == 1 ? strings[0] : strings[0] + "s";
        }
    };

    public static final DurationFormatter EN_SHORT = new WordDurationFormatter() {

        private final EnumMap<DurationField, String> values = new EnumMap<>(DurationField.class);

        {
            values.put(DurationField.NANO, "ns");
            values.put(DurationField.MICRO, "us");
            values.put(DurationField.MILLI, "ms");
            values.put(DurationField.SEC, "s");
            values.put(DurationField.MIN, "m");
            values.put(DurationField.HOUR, "h");
            values.put(DurationField.DAY, "d");
            values.put(DurationField.WEEK, "w");
            values.put(DurationField.MONTH, "mon");
            values.put(DurationField.YEAR, "y");
        }

        @Override
        public String format(long value, DurationField field) {
            return this.values.get(field);
        }
    };

    public static final DurationFormatter RU = new WordDurationFormatter() {

        private final EnumMap<DurationField, String[]> values = new EnumMap<>(DurationField.class);

        {
            values.put(DurationField.NANO, new String[]{" наносекунда", " наносекунды", " наносекунд"});
            values.put(DurationField.MICRO, new String[]{" микросекунда", " микросекунды", " микросекунд"});
            values.put(DurationField.MILLI, new String[]{" миллисекунда", " миллисекунды", " миллисекунд"});
            values.put(DurationField.SEC, new String[]{" секунда", " секунды", " секунд"});
            values.put(DurationField.MIN, new String[]{" минута", " минуты", " минут"});
            values.put(DurationField.HOUR, new String[]{" час", " часа", " часов"});
            values.put(DurationField.DAY, new String[]{" день", " дня", " дней"});
            values.put(DurationField.WEEK, new String[]{" неделя", " недели", " недель"});
            values.put(DurationField.MONTH, new String[]{"месяц", " месяца", " месяцев"});
            values.put(DurationField.YEAR, new String[]{" год", " года", " лет"});
        }

        @Override
        public String format(long value, DurationField field) {
            String[] strings = this.values.get(field);

            return TimeUtil.getEnding(value, strings[0], strings[1], strings[2]);
        }
    };

    /**
     * 'Истекает через: [время в винительном падеже]'
     */
    public static final DurationFormatter RU_ACCUSATIVE = new WordDurationFormatter() {

        private final EnumMap<DurationField, String[]> values = new EnumMap<>(DurationField.class);

        {
            values.put(DurationField.NANO, new String[]{" наносекунду", " наносекунды", " наносекунд"});
            values.put(DurationField.MICRO, new String[]{" микросекунду", " микросекунды", " микросекунд"});
            values.put(DurationField.MILLI, new String[]{" миллисекунду", " миллисекунды", " миллисекунд"});
            values.put(DurationField.SEC, new String[]{" секунду", " секунды", " секунд"});
            values.put(DurationField.MIN, new String[]{" минуту", " минуты", " минут"});
            values.put(DurationField.HOUR, new String[]{" час", " часа", " часов"});
            values.put(DurationField.DAY, new String[]{" день", " дня", " дней"});
            values.put(DurationField.WEEK, new String[]{" неделю", " недели", " недель"});
            values.put(DurationField.MONTH, new String[]{" месяц", " месяца", " месяцев"});
            values.put(DurationField.YEAR, new String[]{" год", " года", " лет"});
        }

        @Override
        public String format(long value, DurationField field) {
            String[] strings = this.values.get(field);

            return TimeUtil.getEnding(value, strings[0], strings[1], strings[2]);
        }
    };

    public static final DurationFormatter RU_SHORT = new WordDurationFormatter() {

        private final EnumMap<DurationField, String> values = new EnumMap<>(DurationField.class);

        {
            values.put(DurationField.NANO, "нс");
            values.put(DurationField.MICRO, "мкс");
            values.put(DurationField.MILLI, "мс");
            values.put(DurationField.SEC, "с");
            values.put(DurationField.MIN, "м");
            values.put(DurationField.HOUR, "ч");
            values.put(DurationField.DAY, "д");
            values.put(DurationField.WEEK, "нед");
            values.put(DurationField.MONTH, "мес");
            values.put(DurationField.YEAR, "г");
        }

        @Override
        public String format(long value, DurationField field) {
            return this.values.get(field);
        }
    };

    // -----

    public void start(StringBuilder builder) {

    }

    public abstract void append(StringBuilder builder, long value, DurationField field);

    public void finish(StringBuilder builder) {

    }

}
