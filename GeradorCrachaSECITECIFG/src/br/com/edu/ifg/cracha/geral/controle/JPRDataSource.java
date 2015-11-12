package br.com.edu.ifg.cracha.geral.controle;

import java.util.Iterator;
import java.util.List;

import org.dom4j.tree.AbstractEntity;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class JPRDataSource<T extends AbstractEntity> implements JRDataSource {

    private Iterator<T> iterator;

    private T beanCorrente;

    public JPRDataSource(List<T> list) {
        this.iterator = list.iterator();
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object bean = beanCorrente;  

        return de.akquinet.jbosscc.needle.reflection.ReflectionUtil.getFieldValue(bean, field.getName());
    }

    @Override
    public boolean next() throws JRException {
        boolean retorno = iterator.hasNext();  
        if(retorno){  
            beanCorrente = iterator.next();  
        }  
        return retorno;  
    }
}