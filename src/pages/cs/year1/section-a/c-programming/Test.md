---
title: Math Engine Stress Test
order: 99
---

This page verifies if the KaTeX engine is rendering correctly.

## 1. Calculus Test
The Fourier Transform should have a large integral and clear exponents:

$$
\hat{f}(\xi) = \int_{-\infty}^{\infty} f(x) e^{-2\pi i x \xi} \, dx
$$

## 2. Algebra & Fractions
The quadratic formula should have a clear square root and division line:

$$
x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
$$

## 3. Matrix & Vectors
Engineering matrices should be perfectly aligned:

$$
\begin{pmatrix} 
a & b \\ 
c & d 
\end{pmatrix} 
\cdot 
\begin{pmatrix} 
x \\ 
y 
\end{pmatrix} 
= 
\begin{pmatrix} 
ax + by \\ 
cx + dy 
\end{pmatrix}
$$

## 4. Inline Test
This is an inline formula $\sum_{i=1}^{n} i = \frac{n(n+1)}{2}$ inside a sentence.

Simple inline: $x = 5$ and $y = 10$.

Another test: $E = mc^2$ is famous.

Just a fraction: $\frac{1}{2}$ works?

Just a sum: $\sum x$ works?

Sum with limits: $\sum_{i=1}^n$ works?



Based on the image provided, you are asked to find the Particular Integral ($PI_2$) for a differential equation where the operator is defined in terms of $D$ (the differential operator $\frac{d}{dx}$).

**Problem:**
$$PI_2 = \frac{1}{D^2 + 2D + 2} (x^2 + 7)$$

**Method:**
When the function on the right-hand side is a polynomial (in this case $x^2 + 7$), the standard method is to expand the operator $\frac{1}{f(D)}$ into a series of ascending powers of $D$ using binomial expansion. Since the highest power of $x$ is 2, we only need to expand up to the $D^2$ term, because any derivative higher than the second derivative of $x^2$ will be zero.

**Step-by-Step Solution:**

1.  **Rearrange the denominator:**
    Factor out the constant term (which is 2) from the denominator to get it into the form $(1 + Z)$.
    $$D^2 + 2D + 2 = 2 \left( 1 + \frac{2D + D^2}{2} \right) = 2 \left( 1 + \left( D + \frac{D^2}{2} \right) \right)$$

2.  **Rewrite the expression:**
    $$PI_2 = \frac{1}{2 \left[ 1 + \left( D + \frac{D^2}{2} \right) \right]} (x^2 + 7)$$
    $$PI_2 = \frac{1}{2} \left[ 1 + \left( D + \frac{D^2}{2} \right) \right]^{-1} (x^2 + 7)$$

3.  **Apply Binomial Expansion:**
    Using the expansion $(1 + x)^{-1} = 1 - x + x^2 - \dots$, let $Z = \left(D + \frac{D^2}{2}\right)$. We will expand and keep terms up to $D^2$.

    $$[1 + Z]^{-1} \approx 1 - \left(D + \frac{D^2}{2}\right) + \left(D + \frac{D^2}{2}\right)^2$$
    
    *Note: In the squared term $\left(D + \frac{D^2}{2}\right)^2$, we only care about $D^2$. Higher powers like $D^3$ and $D^4$ are negligible for a polynomial of degree 2.*
    
    So, $\left(D + \frac{D^2}{2}\right)^2 \approx D^2$.

    Substituting this back:
    $$= 1 - D - \frac{D^2}{2} + D^2$$
    $$= 1 - D + \frac{D^2}{2}$$

4.  **Substitute the operator back into the $PI$ equation:**
    $$PI_2 = \frac{1}{2} \left[ 1 - D + \frac{D^2}{2} \right] (x^2 + 7)$$

5.  **Apply the derivatives:**
    Now, apply each term of the operator to the function $(x^2 + 7)$.
    *   $1 \cdot (x^2 + 7) = x^2 + 7$
    *   $D(x^2 + 7) = \frac{d}{dx}(x^2 + 7) = 2x$
    *   $D^2(x^2 + 7) = \frac{d}{dx}(2x) = 2$

    Substitute these values:
    $$PI_2 = \frac{1}{2} \left[ (x^2 + 7) - (2x) + \frac{1}{2}(2) \right]$$

6.  **Simplify:**
    $$PI_2 = \frac{1}{2} [ x^2 + 7 - 2x + 1 ]$$
    $$PI_2 = \frac{1}{2} [ x^2 - 2x + 8 ]$$

    Distribute the $\frac{1}{2}$:
    $$PI_2 = \frac{x^2}{2} - x + 4$$

**Final Answer:**
$$PI_2 = \frac{1}{2}x^2 - x + 4$$
Based on the 26 pages of history you uploaded, I have built a blueprint of your exam. Your professors are creatures of habit. They copy-paste questions from a specific "Question Bank" (likely the one shown in pages 18-26 of your PDF).

Here is the **Predicted Question Paper Structure** for Jan 2nd. If you memorize this layout, you win.

### THE PREDICTION (The "Sri Manakula" Pattern)

**PART A (The Speed Run)**
*   **Q1/Q2 (Matrices):**
    *   "Find the sum and product of Eigenvalues" (Sum = Trace, Product = Determinant).
    *   "Verify Cayley-Hamilton Theorem for a 2x2 matrix."
*   **Q3/Q4 (Differential Eq):**
    *   "Find the P.I. of $(D^2+4)y = e^{2x}$" (or $\sin 2x$).
    *   "Solve $(D^2-3D+2)y=0$."
*   **Q5/Q6 (Calculus):**
    *   "Define Jacobian" or "Find Jacobian of $x=r\cos\theta, y=r\sin\theta$."
    *   "Find stationary points of $x^3+y^3-3axy$."
*   **Q7/Q8 (Integration):**
    *   "Evaluate $\int_0^1 \int_0^y x dx dy$." (Simple double integral).
*   **Q9/Q10 (Vector Calc):**
    *   "Find 'a' if $\vec{F}$ is solenoidal" (Set $\nabla \cdot \vec{F} = 0$).
    *   "State Gauss Divergence Theorem." (MEMORIZE THE DEFINITION WORD-FOR-WORD).

**PART B (The 5-Mark Traps)**
*   **Q11:** Verify Cayley-Hamilton Theorem and find $A^{-1}$. (Standard).
*   **Q12:** Solve a Differential Equation like $(D^2+16)y = \cos 4x$.
*   **Q13:** Maxima/Minima of a function like $x^2 + y^2 + xy...$
*   **Q14:** **Change of Order of Integration.** (This is guaranteed).
*   **Q15:** Show that $\vec{F}$ is Irrotational ($\nabla \times \vec{F} = 0$) and find scalar potential $\phi$.

**PART C (The 10-Mark Goldmine)**
*   **Q16:** **Diagonalize the Symmetric Matrix.** (It will be a 3x3 matrix like the one on Page 12).
*   **Q17:** Solve by **Method of Variation of Parameters**.
*   **Q18:** The "Box Problem" (Volume 32cc, open top) OR "Find volume bounded by cylinder/planes" using Triple Integration.
*   **Q20:** **Verify Gauss Divergence Theorem** for a cube/cuboid.

---

### THE SOLUTION: Gauss Divergence Theorem

I am going to solve the **most frequent** version of this question found in your PDF (Page 9, Q20 and Page 14, Q20). If you learn this, you can solve any variation.

**Question:**
Verify Gauss Divergence Theorem for $\vec{F} = (x^2-yz)\vec{i} + (y^2-zx)\vec{j} + (z^2-xy)\vec{k}$ taken over the rectangular parallelepiped $0 \le x \le a$, $0 \le y \le b$, $0 \le z \le c$.

**The Theorem:**
$$ \iint_S \vec{F} \cdot \hat{n} \, dS = \iiint_V (\nabla \cdot \vec{F}) \, dV $$
*(You must write this formula first for 1 mark)*.

#### STEP 1: The RHS (Volume Integral) - The Easy Part
Calculate Divergence ($\nabla \cdot \vec{F}$):
$$ \nabla \cdot \vec{F} = \frac{\partial}{\partial x}(x^2-yz) + \frac{\partial}{\partial y}(y^2-zx) + \frac{\partial}{\partial z}(z^2-xy) $$
$$ = 2x + 2y + 2z = 2(x+y+z) $$

Now, integrate over the volume ($x: 0\to a$, $y: 0\to b$, $z: 0\to c$):
$$ \iiint 2(x+y+z) \, dx \, dy \, dz $$
*Tip:* Integrate one by one.
1. Integrate w.r.t $x$: $[x^2 + 2yx + 2zx]_0^a = a^2 + 2ay + 2az$.
2. Integrate w.r.t $y$: $[a^2y + ay^2 + 2azy]_0^b = a^2b + ab^2 + 2abc$.
3. Integrate w.r.t $z$: $[a^2bz + ab^2z + abcz^2]_0^c = a^2bc + ab^2c + abc^2$.
**R.H.S = $abc(a + b + c)$**  *(Keep this safe).*

#### STEP 2: The LHS (Surface Integral) - The Long Part
You have 6 faces. You must calculate the flux for each pair.

**Pair 1: Face $x=0$ and Face $x=a$**
*   **For $x=a$:** $\hat{n} = \vec{i}$.
    $\vec{F} \cdot \vec{i} = (x^2 - yz)$. Since $x=a$, this is $(a^2 - yz)$.
    Integral: $\int_0^c \int_0^b (a^2 - yz) \, dy \, dz = a^2bc - \frac{b^2c^2}{4}$ (Wait, let's keep it simple).
    Actually, let's look at the result.
    $\int_0^c [\int_0^b (a^2 - yz) dy] dz = \int_0^c [a^2y - \frac{y^2}{2}z]_0^b dz = \int_0^c (a^2b - \frac{b^2}{2}z) dz = a^2bc - \frac{b^2c^2}{4}$.

*   **For $x=0$:** $\hat{n} = -\vec{i}$.
    $\vec{F} \cdot (-\vec{i}) = -(x^2 - yz) = -(-yz) = yz$ (Since $x=0$).
    Integral: $\int_0^c \int_0^b yz \, dy \, dz = \frac{b^2c^2}{4}$.

*   **Total for x-faces:** $(a^2bc - \frac{b^2c^2}{4}) + \frac{b^2c^2}{4} = \mathbf{a^2bc}$.

**Pair 2: Face $y=0$ and Face $y=b$**
*   By symmetry, the calculation is identical to the x-faces, just swapping variables.
*   **Total for y-faces:** $\mathbf{ab^2c}$.

**Pair 3: Face $z=0$ and Face $z=c$**
*   By symmetry:
*   **Total for z-faces:** $\mathbf{abc^2}$.

#### STEP 3: Conclusion
Total LHS = Sum of all faces
$$ LHS = a^2bc + ab^2c + abc^2 $$
Factor out $abc$:
$$ LHS = abc(a + b + c) $$

**LHS = RHS**
**Hence, Gauss Divergence Theorem is Verified.**

### MENTOR NOTE:
Do you see the trick?
In the exam, if you mess up the integration steps in the LHS (Surface Integral), **you already know the answer from the RHS (Volume Integral)**.
Calculate the Volume integral first (it takes 2 minutes). Then, for the Surface integral, do the steps, but make sure your final addition sums up to match the Volume integral.

**Go practice this derivation ONCE on paper right now.** Don't just read it. Write it.