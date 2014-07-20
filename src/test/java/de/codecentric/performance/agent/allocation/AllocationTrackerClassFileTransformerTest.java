package de.codecentric.performance.agent.allocation;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.codecentric.test.TestBean;

public class AllocationTrackerClassFileTransformerTest {

  /**
   * The object under test
   */
  private AllocationTrackerClassFileTransformer transformer;

  @Before
  public void setUp() {
    transformer = new AllocationTrackerClassFileTransformer("de.codecentric.test");
  }

  @Test
  public void allocationTrackerClassesAreIgnored() throws Exception {
    byte[] classBytes = getClassBytes(ConstructorVisitor.class);
    byte[] actual = transformer.transform(null, ConstructorVisitor.class.getName(), ConstructorVisitor.class, null,
        classBytes);

    assertSame(classBytes, actual);
  }

  @Test
  public void notMatchingClassPrefixesAreIgnored() throws Exception {
    byte[] classBytes = getClassBytes(Assert.class);
    byte[] actual = transformer.transform(null, Assert.class.getName(), Assert.class, null, classBytes);

    assertSame(classBytes, actual);
  }

  @Test
  public void matchingClassesAreExtended() throws Exception {
    byte[] byteArray = getClassBytes(TestBean.class);
    byte[] actual = transformer.transform(null, TestBean.class.getName(), TestBean.class, null, byteArray);

    // the class has been changed. What else can we do?
    assertThat(actual.length, is(greaterThan(byteArray.length)));
  }

  private byte[] getClassBytes(Class<?> clazz) throws IOException {
    String className = clazz.getName();
    String classAsPath = className.replace('.', '/') + ".class";
    InputStream stream = clazz.getClassLoader().getResourceAsStream(classAsPath);
    
    return IOUtils.toByteArray(stream);
  }
}
