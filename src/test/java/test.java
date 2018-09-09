import com.github.lordrex34.reflection.util.ClassPathUtil;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;

/**
 * 功能描述：
 *
 * @author Yupi Li
 * @date 2018/9/1 23:37
 */

public class test {
    @Test
    public void test() throws IOException {
        FluentIterable<Class<?>> allClasses = ClassPathUtil.getAllClasses("com.yupi.controller");
        ImmutableList<Class<?>> classes = allClasses.toList();
    }
}
