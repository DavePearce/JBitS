package jbits.core;

import jblob.core.Blob;

/**
 * <p>
 * Provides core interfaces for describing, representing and manipulating
 * content. We can view the mechanism as a stack, where a raw byte array
 * represents the lowest level:
 * </p>
 *
 * <pre>
 *
 *       +--------------+
 *       |  |           |                   (byte array proxy)
 *       +--------------+
 *       :              :
 *       :              :
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                                      | (byte blob)
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+
 * :                                      :
 * :                                      :
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |00|0A|01|00|00|00|F0|FF|00|00|00|00|01| (raw byte array)
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+
 *  00 01 02 03 04 05 06 07 08 09 10 11 12
 * </pre>
 *
 * <p>
 * In the above example, we have a bytearray at the very bottom providing the
 * raw content we are working with. This is enclosed inside a
 * <code>ByteBlob</code> which provides an implementation of
 * <code>Blob</code>. A blob is essentially abstracts a sequence of
 * binary data. Thus, a blob can be ground in an array (as above) or built on
 * top of another blob. The latter allows one to modify a blob (such as
 * inserting or removing bytes, etc).
 * </p>
 *
 * <p>
 * Finally, at the top of stack we have the proxy object(s). These provides
 * handy interfaces to working with raw blobs. Specifically, a proxy object
 * corresponds to some form of data organisation in the underlying blob. In the
 * above example, we have a <code>ByteArray</code> proxy which treats the
 * underlying data as an array.
 * </p>
 *
 * @author David J. Pearce
 *
 */
public class Content {

	/**
	 *
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface Proxy {
		/**
		 * Get the offset of this proxy object within the blob containing it.
		 *
		 * @return
		 */
		public int getOffset();
		/**
		 * Get the underlying blob containing this proxy object.
		 *
		 * @return
		 */
		public Blob getBlob();

		/**
		 * Get the layout represented by this proxy.
		 *
		 * @return
		 */
		public Content.Layout<?> getLayout();

		/**
		 * Get the size of the underlying item in bytes.
		 *
		 * @return
		 */
		public int sizeOf();

		/**
		 * Convert proxy into a byte sequence.
		 * @return
		 */
		public byte[] toBytes();
	}

	/**
	 * Provides a generic mechanism for describing the data layout of a piece of
	 * content.
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface Layout<T> {

		/**
		 * Return the size (in bytes) of this layout instantiation in a given blob.
		 *
		 * @param blob   The blob in which this layout is instantiated.
		 * @param offset The starting offset where this layout is instantiated.
		 * @return
		 */
		public int sizeOf(Blob blob, int offset);

		/**
		 * Read the value representing an instantiation of this layout at a given
		 * position in a blob.
		 *
		 * @param blob   The blob containing the instantiation of this layout.
		 * @param offset The offset within the blob where the instantiation of this
		 *               layout begins.
		 * @return
		 */
		public T read(Blob blob, int offset);

		/**
		 * Update the value representing an instantiation of this layout at a given
		 * position in a blob. Observe that this <i>overwrites</i> the existing value
		 * and does not otherwise affect the blob's size.
		 *
		 * @param proxy  The value to be written.
		 * @param blob   The blob containing the instantiation of this layout.
		 * @param offset The offset within the blob where the instantiation of this
		 *               layout begins.
		 * @return
		 */
		public Blob write(T proxy, Blob blob, int offset);

		/**
		 * Insert a value representing an instantiation of this layout at a given
		 * position in a blob. Observe that this necessarily increases the blob's size.
		 *
		 * @param proxy  The value to be written.
		 * @param blob   The blob containing the instantiation of this layout.
		 * @param offset The offset within the blob where the instantiation of this
		 *               layout begins.
		 * @return
		 */
		public Blob insert(T proxy, Blob blob, int offset);
	}

	/**
	 * Represents a layout whose size is statically known. Knowing that a child
	 * layout is static can help in some cases. Unfortunately, not all layouts can
	 * be static by definition.
	 *
	 * @author David J. Pearec
	 *
	 */
	public interface StaticLayout<T> extends Layout<T> {
		/**
		 * Return the size of this layout in bytes. Since this is a static layout, there
		 * is no need for any parameters!
		 *
		 * @return
		 */
		public int sizeOf();
	}

	/**
	 * A specialised interface which can be used to construct an object proxy for a
	 * given position within a blob.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public interface Constructor<L,T> {
		public T apply(L layout, Blob blob, int offset);
	}


}
