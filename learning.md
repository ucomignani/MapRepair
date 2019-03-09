Learning the user preferences
=============================

In this document, we explain how the preference function used by our prototype can be learned by relying on off-the-shelf machine learning algorithms and based on training sets in which former user choices have been recorded.

In order to be able to rank the possible repairs and use inference on previously specified user’s choices, we first need to define suitable metrics for comparing each pair of repairs *μ*<sub>*r*<sub>1</sub></sub> and *μ*<sub>*r*<sub>2</sub></sub> for a given s-t tgd *μ*. In case the number of possible repairs for *μ* is greater than two, the metrics will compare the repairs pair by pair. Since our algorithm modifies the exported and the repeated variables in the bodies of the s-t tgds, we ground our metrics on the following two parameters:

-   *Δ*<sub>*FV*</sub> amounting to the difference between the number of exported variables in *μ*<sub>*r*<sub>1</sub></sub> and *μ*<sub>*r*<sub>2</sub></sub>

-   *Δ*<sub>*J*</sub> corresponding to the difference between the number of joins in the bodies of *μ*<sub>*r*<sub>1</sub></sub> and *μ*<sub>*r*<sub>2</sub></sub>.

The key intuition behind applying statistical learning to rank possible safe rewritings of s-t tgds is to leverage an available training set of correctly identified observations. Each observation consists of the two metrics *Δ*<sub>*FV*</sub> and *Δ*<sub>*J*</sub> defined above along with the s-t tgd that better fits the values *Δ*<sub>*FV*</sub> and *Δ*<sub>*J*</sub>. More formally, we define a vector of variables **X** = ⟨*Δ*<sub>*FV*</sub>, *Δ*<sub>*J*</sub>⟩ and we use it as input to train the learning model. We define a qualitative output *G* embodying the s-t tgd *μ*<sub>*r*<sub>1</sub></sub> and *μ*<sub>*r*<sub>2</sub></sub> chosen by the preference function pref(). We next denote by ⟨*δ*<sub>*FV*</sub>, *δ*<sub>*J*</sub>⟩ the observed values of the vector **X** and by *g* the actual value of the qualitative output *G*. Let G<sub>pred</sub> be the prediction associated to *G* obtained by the learning model. The goal of the learning model is to obtain, for each observation ⟨*δ*<sub>*FV*</sub>, *δ*<sub>*J*</sub>⟩ of **X**, a predicted value g<sub>*pred</sub> of G<sub>*pred</sub> that fits the ground-truth value *g* corresponding to ⟨*δ*<sub>*FV*</sub>, *δ*<sub>*J*</sub>⟩.

We employ the k-NN classification algorithm as the supervised learning method for learning pref(). This algorithm has been chosen due to its flexibility in comparing a new item to classify with the existing items in the training set, which are the k-nearest to the former in terms of their similarity.

In our setting, training sets consist of measurements {(⟨*δ*<sub>*FV*, *i*</sub>, *δ*<sub>*J*, *i*</sub>⟩,*g*<sub>*i*</sub>)|*i* = 1, …, *N*}, such that ⟨*δ*<sub>*FV*, *i*</sub>, *δ*<sub>*J*, *i*</sub>⟩ are values of **X** on the *i*-th measurement, and *g*<sub>*i*</sub> is the value of *G* on the *i*-th measurement. Intuitively, measurements have been built by choosing the repair that corresponds to the s-t tgd that best fits the ground truth. 
Consider, for example, the s-t tgd :

*μ*<sub>1</sub> = R<sub>1</sub>(x, y, z) ∧ S<sub>1</sub>(y, z, z) → T_1(x, z)

 and its three possible repairs :
 * *r*<sub>1</sub> =  R<sub>1</sub>(x, y, z<sub>1</sub>) ∧ S<sub>1</sub>(y, z<sub>1</sub>, z<sub>1</sub>) → T<sub>1</sub>(x)
 * *r*<sub>1</sub> =  R<sub>1</sub>(x, y, z<sub>1</sub>) ∧ S<sub>1</sub>(y, z<sub>2</sub>, z) → T<sub>1</sub>(x, z)
 * *r*<sub>3</sub> =  R<sub>1</sub>(x, y, z<sub>1</sub>) ∧ S<sub>1</sub>(y, z, z) → T<sub>1</sub>(x, z)
 
 In order to compare the possibles pairs of repairs, suppose that the preference goes to the repair with the maximum number of exported variables and, in case of equality (i.e., *δ*<sub>*FV*</sub> = 0), the preference goes to the repair with the maximum number of joins. The above leads to a training set with three measurements ⟨1, −1, *r*<sub>2</sub>⟩, ⟨1, 0, *r*<sub>3</sub>⟩ and ⟨0, 1, *r*<sub>3</sub>⟩. The first observation, which corresponds to the comparison between *r*<sub>1</sub> and *r*<sub>2</sub> leads to computation of values 1 and -1, choosing, thus, *r*<sub>2</sub>; the second observation, which corresponds to the comparison between *r*<sub>1</sub> and *r*<sub>2</sub>, leads to computation of values 1 and 0, choosing, thus, *r*<sub>3</sub>; the third observation, which corresponds to the comparison between *r*<sub>2</sub> and *r*<sub>3</sub>, leads to computation of values 0 and 1, choosing, thus, *r*<sub>3</sub>.

Intuitively, as shown in the previous example, the measurements are built by choosing the repair that corresponds to the s-t tgds that are the closest to the golden standard one. Using this training set and given an input ⟨*δ*<sub>*FV*</sub>, *δ*<sub>*J*</sub>⟩, a predicted value g<sub>*pred</sub> is computed with the use of the k-NN method. More precisely, the k-NN method finds the k-nearest measurements to ⟨*δ*<sub>*FV*, *j*</sub>, *δ*<sub>*J*, *j*</sub>⟩ among the measurements of the training set. The adopted similarity function is the Euclidean distance.
