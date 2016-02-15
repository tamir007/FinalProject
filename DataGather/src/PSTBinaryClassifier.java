import java.util.Map;
import java.util.HashMap;

public class PSTBinaryClassifier {

	private String 				_context;
	private Map<String, Double> _g;
	private Double[] 			_w;
	private Double[]		 	_xt;
	private double				_ht;
	private int					_t;
	
	public PSTBinaryClassifier(int featureLength){
		
		_context = new String();
		_g = new HashMap<String, Double>();
		_w = new Double[featureLength];
		_t = 0;
		
		for (int i = 0; i < featureLength; ++i)
			_w[i] = 0.0d;
		
		_g.put("", 0.0d);
	}
	
	public char predict(Double[] x){
		
		++_t;
		_xt = x;
		
		// Calculate j
		int j = 0;
		if (_context.length() > 0){
			while (_g.containsKey(_context.substring(_context.length() - 1 - j)))
				++j;
		}
		double gSum = 0.0d;
		for (int i = 0; i < j; ++i)
			gSum += Math.pow(2, (-1)*i/2) * _g.get(_context.substring(_context.length() - 1 - i));
		
		_ht = Utils.dotProduct(_w, _xt) + gSum;

		if (_ht == 0)
			return '?';
		else
			return (_ht > 0 ? '+' : '-');
	}

	public void feedback(char labelSign){
		
		int labelValue = (labelSign == '+' ? 1 : -1);
		
		double loss = Math.max(0, 1 - labelValue * _ht);
		
	//	double tau = loss / (Math.pow((Utils.norm(_xt)), 2) + 3);
		
		int dt = _t - 1;
		
		// Updating the weights vector
	//	_w = Utils.addVectors(_w, Utils.multScalar(_xt, labelValue * tau));
		
		// Adding nodes to the tree
		String s = "";
		for (int i = 1; i <= dt; ++i){
			
			s = _context.substring(_context.length() - i);
			if (!_g.containsKey(s))
				_g.put(s, 0.0d);
			
			_g.put(s, _g.get(s) + labelValue * Math.pow(2, -1*s.length() / 2.0d) /* * tau*/);
		}
		_context += labelSign;
	}
}
