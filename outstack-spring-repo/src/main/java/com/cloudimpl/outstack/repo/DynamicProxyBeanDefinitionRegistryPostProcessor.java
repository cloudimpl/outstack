package com.cloudimpl.outstack.repo;

import com.cloudimpl.outstack.repo.core.ReactiveRepository;
import com.cloudimpl.outstack.repo.core.Repository;
import com.cloudimpl.outstack.repo.core.geo.BaseRepository;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;


/**
 * Component which dynamically creates BeanDefinitions for dynamic proxy beans
 */
@Configuration
public class DynamicProxyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

	public DynamicProxyBeanDefinitionRegistryPostProcessor() {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		ScanResult scanResult = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan();
		ClassInfoList controlClasses = scanResult.getClassesImplementing(BaseRepository.class.getName());
		for(Class<?> type : controlClasses.loadClasses())
		{
			if(type.isInterface())
			{
				BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(type)
						.setLazyInit(true)
				.setScope(BeanDefinition.SCOPE_SINGLETON);

				builder.addConstructorArgValue(type);

				builder.setFactoryMethodOnBean(
						"createDynamicProxyBean",
						DynamicProxyBeanFactory.DYNAMIC_PROXY_BEAN_FACTORY
				);

				registry.registerBeanDefinition(type.getName(), builder.getBeanDefinition());
			}
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
