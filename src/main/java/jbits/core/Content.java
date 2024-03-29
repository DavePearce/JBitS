package jbits.core;

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
 * <code>Byte.Blob</code> which provides an implementation of
 * <code>Content.Blob</code>. A blob is essentially abstracts a sequence of
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
		public Content.Blob getBlob();

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
		public int sizeOf(Content.Blob blob, int offset);

		/**
		 * Read the value representing an instantiation of this layout at a given
		 * position in a blob.
		 *
		 * @param blob   The blob containing the instantiation of this layout.
		 * @param offset The offset within the blob where the instantiation of this
		 *               layout begins.
		 * @return
		 */
		public T read(Content.Blob blob, int offset);

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
		public Content.Blob write(T proxy, Content.Blob blob, int offset);

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
		public Content.Blob insert(T proxy, Content.Blob blob, int offset);
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
		public T apply(L layout, Content.Blob blob, int offset);
	}

	/**
	 * Represents an immutable binary blob of data representing a piece of content..
	 * Blobs are immutable data structures which, when written, construct new blobs.
	 * Blobs are also elastic in that they automatically resize to accommodate
	 * writing beyond their current bounds.
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface Blob {
		/**
		 * Get the total number of bytes in this blob.
		 *
		 * @return
		 */
		public int size();

		/**
		 * Get the complete contents of this value as a sequence of bytes.
		 *
		 * @return
		 */
		public byte[] readAll();

		/**
		 * Read a given byte from a given position in the blob. The index must be
		 * within bounds.
		 *
		 * @param index
		 * @return
		 */
		public byte readByte(int index);

		/**
		 * Read a 16-bit signed integer starting at a given position in the blob
		 * assuming big endian orientation. All indices must be entirely within bounds.
		 *
		 * @param index
		 * @return
		 */
		public short readShort(int index);

		/**
		 * Read a 32-bit signed integer starting at a given position in the blob
		 * assuming big endian orientation. All indices must be entirely within bounds.
		 *
		 * @param index
		 * @return
		 */
		public int readInt(int index);

		/**
		 * Read a given sequence of bytes from a given position in the blob. The entire
		 * region must be within bounds.
		 *
		 * @param index
		 * @param length
		 * @return
		 */
		public byte[] readBytes(int index, int length);

		/**
		 * Read a given sequence of bytes from a given position in the blob into a
		 * prexisting array. The entire region must be within bounds.
		 *
		 * @param index     The index within this blob to start reading from
		 * @param length    The number of bytes to read
		 * @param dest      The destination byte array
		 * @param destStart starting position within destination
		 * @return
		 */
		public void readBytes(int index, int length, byte[] dest, int destStart);

		/**
		 * Write a given byte to a given position within this value. The index does not
		 * need to be in bounds since blobs are elastic. Thus, writing beyond bounds
		 * increases the size of the blob accordingly.
		 *
		 * @param index Position to overwrite
		 * @param b     data byte to written
		 * @return
		 */
		public Diff writeByte(int index, byte b);

		/**
		 * Write a given 16-bit signed integer byte to a given position within this blob
		 * assuming a big endian orientation. The index does not need to be in bounds
		 * since blobs are elastic. Thus, writing beyond bounds increases the size of
		 * the blob accordingly.
		 *
		 * @param index Position to overwrite
		 * @param b     data byte to written
		 * @return
		 */
		public Diff writeShort(int index, short i16);

		/**
		 * Write a given 32-bit signed integer byte to a given position within this blob
		 * assuming a big endian orientation. The index does not need to be in bounds
		 * since blobs are elastic. Thus, writing beyond bounds increases the size of
		 * the blob accordingly.
		 *
		 * @param index Position to overwrite
		 * @param b     data byte to written
		 * @return
		 */
		public Diff writeInt(int index, int i32);

		/**
		 * Replace a given section of this value with a new sequence of bytes. The index
		 * does not need to be in bounds since blobs are elastic. Thus, writing beyond
		 * bounds increases the size of the blob accordingly.
		 *
		 * @param index Position to overwrite
		 * @param b     data byte to written
		 * @return
		 */
		public Diff writeBytes(int index, byte... bytes);

		/**
		 * Replace a given section of this value with a new sequence of bytes. The new
		 * byte sequence does not need to be the same length as the section replaced.
		 * The index does not need to be in bounds since blobs are elastic. Thus,
		 * writing beyond bounds increases the size of the blob accordingly.
		 *
		 * @param index  starting offset of section being replaced
		 * @param length size of section being replaced
		 * @param b      data byte to written
		 * @return
		 */
		public Diff replaceBytes(int index, int length, byte... bytes);

		/**
		 * Insert a given byte to a given position within this value. The index does not
		 * need to be in bounds since blobs are elastic. Thus, inserting beyond bounds
		 * increases the size of the blob accordingly.
		 *
		 * @param index Position to insert before
		 * @param b     data byte to written
		 * @return
		 */
		public Diff insertByte(int index, byte b);

		/**
		 * Insert a given 16-bit signed integer byte to a given position within this blob
		 * assuming a big endian orientation. The index does not need to be in bounds
		 * since blobs are elastic. Thus, insert beyond bounds increases the size of
		 * the blob accordingly.
		 *
		 * @param index Position to insert before
		 * @param b     data byte to written
		 * @return
		 */
		public Diff insertShort(int index, short i16);

		/**
		 * Insert a given 32-bit signed integer byte to a given position within this blob
		 * assuming a big endian orientation. The index does not need to be in bounds
		 * since blobs are elastic. Thus, insert beyond bounds increases the size of
		 * the blob accordingly.
		 *
		 * @param index Position to insert before
		 * @param b     data byte to written
		 * @return
		 */
		public Diff insertInt(int index, int i32);

		/**
		 * Insert a given section of this value with a new sequence of bytes. The index
		 * does not need to be in bounds since blobs are elastic. Thus, insert beyond
		 * bounds increases the size of the blob accordingly.
		 *
		 * @param index Position to insert before
		 * @param b     data byte to written
		 * @return
		 */
		public Diff insertBytes(int index, byte... bytes);

		/**
		 * Merge two (disjoint) siblings together into one. For this to be valid, they
		 * must either: both have the same parent; or one is the immediate parent of the
		 * other.
		 *
		 * @param parent
		 * @return
		 */
		public Content.Blob merge(Content.Blob sibling);

	}

	/**
	 * Replacements cannot overlap.
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface Diff extends Blob {
		/**
		 * Get the parent of this blob, if one exists.
		 *
		 * @return
		 */
		public Blob parent();

		/**
		 * Get the number of replacements in this diff.
		 *
		 * @return
		 */
		public int count();

		/**
		 * Get a specific replacement.
		 * @return
		 */
		public Replacement getReplacement(int i);

	}

	public interface Replacement {
		/**
		 * Get the starting offset of this replacement.
		 * @return
		 */
		public int offset();

		/**
		 * Get the length of this replacement.
		 *
		 * @return
		 */
		public int size();

		/**
		 * Get the array of replacement bytes.
		 *
		 * @return
		 */
		public byte[] bytes();
	}
}
