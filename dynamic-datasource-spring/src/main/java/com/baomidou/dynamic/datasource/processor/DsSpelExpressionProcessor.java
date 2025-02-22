/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * SpEL表达式处理器
 *
 * @author TaoYu
 * @since 2.5.0
 */
public class DsSpelExpressionProcessor extends DsProcessor {

    /**
     * 参数发现器
     */
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    /**
     * Express语法解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    /**
     * 解析上下文的模板
     * 对于默认不设置的情况下,从参数中取值的方式 #param1
     * 设置指定模板 ParserContext.TEMPLATE_EXPRESSION 后的取值方式: #{#param1}
     * issues: https://github.com/baomidou/dynamic-datasource-spring-boot-starter/issues/199
     * issues: https://github.com/baomidou/dynamic-datasource-spring-boot-starter/issues/485
     */
    private ParserContext parserContext = ParserContext.TEMPLATE_EXPRESSION;
    private BeanResolver beanResolver;

    @Override
    public boolean matches(String key) {
        return true;
    }

    @Override
    public String doDetermineDatasource(MethodInvocation invocation, String key) {
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();
        ExpressionRootObject rootObject = new ExpressionRootObject(method, arguments, invocation.getThis());
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, arguments, NAME_DISCOVERER);
        context.setBeanResolver(beanResolver);
        return PARSER.parseExpression(key, parserContext).getValue(context, String.class);
    }

    /**
     * 设置解析上下文
     *
     * @param parserContext 解析上下文
     */
    public void setParserContext(ParserContext parserContext) {
        this.parserContext = parserContext;
    }

    /**
     * 设置bean解析器
     *
     * @param beanResolver bean解析器
     */
    public void setBeanResolver(BeanResolver beanResolver) {
        this.beanResolver = beanResolver;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ExpressionRootObject {
        private final Method method;

        private final Object[] args;

        private final Object target;
    }
}