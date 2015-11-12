package br.com.edu.ifg.cracha.geral.controle;


import java.util.Iterator;
import java.util.Vector;

import br.com.edu.ifg.cracha.geral.modelo.ModeloCracha;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class CrachaJRDataSource implements JRDataSource{
    
    private Iterator<ModeloCracha> iterator;
    private ModeloCracha mC;
    
    public CrachaJRDataSource(Vector<ModeloCracha> vMC) {
        super();
        iterator = vMC.iterator();
    }
    
    @Override
    public boolean next() throws JRException {
        boolean retorno = iterator.hasNext();
        if (retorno) {
            mC = iterator.next();
        }
        return retorno;
    }

    @Override
    public Object getFieldValue(JRField jrf) throws JRException {
        if (jrf.getName().equals("nome_partic")) {
            return mC.getNome();
        }
        if (jrf.getName().equals("tipo")) {
            return mC.getTipo();
        }
        if (jrf.getName().equals("url")) {
            return mC.getURL();
        }
        if(jrf.getName().equals("nome_simplif")) {
            return mC.getNomeSimplif();
        }
        return null;
    }    
}